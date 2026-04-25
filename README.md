<img src="https://raw.githubusercontent.com/amitshekhariitbhu/Learn-Kotlin-Coroutines/main/assets/learn-kotlin-coroutines.png" alt="Learn Kotlin Coroutines banner" />

# 我是如何把一个传统 Android 协程示例，重构成 Clean Architecture + Koin DI 项目的

很多协程教程项目都有一个共同特点：功能能跑，示例也清楚，但工程结构往往停留在“为了演示而演示”的阶段。

比如：

- `Activity` 负责组装依赖
- `ViewModel` 直接调用网络层和数据库层
- 异常处理散落在每个协程块里
- 新增一个页面，就要继续复制一套依赖创建逻辑

这类项目很适合入门，但如果继续往真实业务开发靠，就会很快暴露出结构问题。

我这次做的事情，不是单纯给项目“加一点优化”，而是把这个原本偏传统写法的 Android Kotlin 协程示例，完整地往工程化方向推进了一步：

- 从没有依赖注入，改成 `Koin DI`
- 从 `ViewModel` 直连数据源，改成 `UseCase + Repository`
- 从职责混杂的表现层，改成 `UI -> Domain -> Data` 分层
- 从分散处理异常，改成 `Repository` 统一返回 `Resource`

这篇 README 也不再只是项目说明，而是按一次真实重构过程来写清楚：

- 原来的问题是什么
- 为什么这些问题值得改
- 我是怎么一步步改成现在这样的
- 改完之后，代码结构到底发生了什么变化

如果你也正在维护一个“能跑，但不太好继续扩展”的 Android 示例项目，这篇文章的重点不是告诉你某个框架有多高级，而是给出一条足够务实的重构路径。

---

## 一、先说原来的问题：它能运行，但不适合继续长大

传统协程示例项目最大的问题，不是“代码错了”，而是“结构开始失控了”。

最典型的情况，就是没有依赖注入时，依赖关系往往只能靠手动管理。

项目里保留的旧版 `ViewModelFactory` 很能说明问题：

```kotlin
class ViewModelFactory(
    private val apiHelper: ApiHelper,
    private val dbHelper: DatabaseHelper
) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SingleNetworkCallViewModel::class.java)) {
            return SingleNetworkCallViewModel(apiHelper, dbHelper) as T
        }
        if (modelClass.isAssignableFrom(SeriesNetworkCallsViewModel::class.java)) {
            return SeriesNetworkCallsViewModel(apiHelper, dbHelper) as T
        }
        // 每新增一个 ViewModel，都要继续追加判断
        throw IllegalArgumentException("Unknown class name")
    }
}
```

刚开始页面少的时候，这种写法似乎问题不大。但只要项目继续扩展，问题会立刻出现：

- 每新增一个 `ViewModel`，都要改 Factory
- Factory 会变成一个越来越大的分发中心
- 页面层知道太多对象创建细节
- 测试时很难优雅地替换真实依赖

本质上，这不是“写法繁琐”那么简单，而是依赖关系没有被正确建模。

一句话总结这一阶段的问题：

> 代码的问题不在于它不能运行，而在于它无法以低成本继续演进。

---

## 二、第一个改造点：先把依赖注入补上

如果依赖关系本身就是分散、手动、硬编码的，那后面的架构优化很容易流于表面。

所以这次重构，我先处理的是依赖管理问题。

### 1. 重构前：对象靠页面或 Factory 手动拼装

没有 DI 时，页面层通常需要间接承担依赖创建责任。即便不是直接 `new`，也会通过 `ViewModelFactory` 把依赖一路传进去。

这种模式最大的问题有两个：

- 对象创建逻辑和业务展示逻辑混在一起
- 依赖关系散落，扩展和测试成本都高

### 2. 重构后：把依赖集中交给 Koin

现在项目里，依赖关系被集中声明在模块中：

```kotlin
val appModule = module {
    single { RetrofitBuilder.apiService }
    single<ApiHelper> { ApiHelperImpl(get()) }
    single { DatabaseBuilder.getInstance(androidContext()) }
    single<DatabaseHelper> { DatabaseHelperImpl(get()) }
    single<UserRepository> { UserRepositoryImpl(get(), get()) }

    factory { GetUsersUseCase(get()) }
    factory { GetMoreUsersUseCase(get()) }
    factory { GetUsersFromDbUseCase(get()) }
    factory { GetUsersWithErrorUseCase(get()) }
    factory { GetUsersSeriesUseCase(get()) }
    factory { GetUsersParallelUseCase(get()) }
}

val viewModelModule = module {
    viewModel { SingleNetworkCallViewModel(get()) }
    viewModel { SeriesNetworkCallsViewModel(get()) }
    viewModel { ParallelNetworkCallsViewModel(get()) }
    viewModel { RoomDBViewModel(get()) }
}
```

然后在应用启动时统一初始化：

```kotlin
class CoroutinesApp : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@CoroutinesApp)
            modules(listOf(appModule, viewModelModule))
        }
    }
}
```

页面层也终于可以回到它该有的样子：

```kotlin
class SingleNetworkCallActivity : AppCompatActivity() {

    private val viewModel: SingleNetworkCallViewModel by viewModel()
}
```

这一步改完之后，项目最大的变化不是“少写了几个对象创建代码”，而是依赖关系第一次变得清晰了：

- 页面只声明自己需要什么
- 对象创建由容器统一管理
- 依赖边界集中可见
- 后续做分层时，注入链路已经打通

换句话说，依赖注入不是装饰品，它是这次重构真正的起点。

如果只从工程收益来看，这一步至少解决了三件事：

- 去掉了手动拼装依赖的重复劳动
- 给后续分层改造提供了稳定注入入口
- 让“依赖关系”第一次变成了一个可以集中维护的系统

---

## 三、第二个改造点：让 ViewModel 不再直接碰底层数据源

很多传统 Android 项目里，`ViewModel` 经常会逐渐变成“什么都做一点”的角色。

比如它既要发起协程，又要调用 API，又要访问数据库，还要处理异常、拼装 UI 状态。页面少的时候问题不明显，一旦场景变多，`ViewModel` 就会越来越重。

### 1. 重构前：ViewModel 直接依赖 ApiHelper / DatabaseHelper

这类写法非常常见：

```kotlin
class SingleNetworkCallViewModel(
    private val apiHelper: ApiHelper,
    private val dbHelper: DatabaseHelper
) : ViewModel() {

    private fun fetchUsers() {
        viewModelScope.launch {
            val users = apiHelper.getUsers()
            // 直接在 ViewModel 中处理数据源调用
        }
    }
}
```

问题在于，这样的 `ViewModel` 已经知道了太多底层实现：

- 它知道数据来自哪里
- 它知道应该调用哪个接口
- 它知道如何处理异常
- 它还要负责把结果转换成 UI 状态

这其实违背了表现层应有的职责边界。

### 2. 重构后：ViewModel 只依赖 UseCase

现在的 `SingleNetworkCallViewModel` 只依赖业务用例：

```kotlin
class SingleNetworkCallViewModel(
    private val getUsersUseCase: GetUsersUseCase
) : ViewModel() {

    private fun fetchUsers() {
        viewModelScope.launch {
            uiState.postValue(UiState.Loading)
            when (val result = getUsersUseCase()) {
                is Resource.Success -> uiState.postValue(UiState.Success(result.data))
                is Resource.Error -> uiState.postValue(UiState.Error(result.message))
            }
        }
    }
}
```

对应的业务逻辑被下沉到 `UseCase`：

```kotlin
class GetUsersUseCase(private val userRepository: UserRepository) {

    suspend operator fun invoke(): Resource<List<ApiUser>> {
        return userRepository.getUsers()
    }
}
```

这一步改造的关键，不只是“多加了一层”。

真正的价值在于：

- `ViewModel` 不再直接感知网络层和数据库层
- 业务逻辑有了明确归属
- 同一类业务流程更容易被复用
- 表现层开始真正只关心“结果怎么展示”

很多人觉得 `UseCase` 是“看起来更高级”的写法，但如果项目已经进入多个页面、多种请求场景并存的状态，它其实是非常务实的拆分。

这里可以用一句更直接的话概括：

> ViewModel 不应该决定业务怎么做，它应该决定结果怎么展示。

---

## 四、第三个改造点：把异常处理从 UI 层拿下去

如果一个项目里的每个 `ViewModel` 都写着自己的 `try-catch`，那通常意味着异常处理还停留在“哪里报错就在哪里兜底”的阶段。

这种方式当然也能工作，但它会带来两个后果：

- 每个页面都在重复写几乎一样的错误处理逻辑
- 错误建模不统一，UI 层承担了太多本不该承担的责任

### 1. 重构前：try-catch 散落在每个 ViewModel 里

典型写法通常是这样：

```kotlin
viewModelScope.launch {
    uiState.postValue(UiState.Loading)
    try {
        val users = apiHelper.getUsers()
        uiState.postValue(UiState.Success(users))
    } catch (e: Exception) {
        uiState.postValue(UiState.Error(e.toString()))
    }
}
```

这段代码看起来没问题，但从工程角度看，它有明显缺陷：

- 异常处理策略重复
- ViewModel 职责膨胀
- UI 层和底层错误强耦合

### 2. 重构后：Repository 统一返回 Resource

现在异常被统一下沉到 `Repository`：

```kotlin
class UserRepositoryImpl(
    private val apiHelper: ApiHelper,
    private val databaseHelper: DatabaseHelper
) : UserRepository {

    override suspend fun getUsers(): Resource<List<ApiUser>> {
        return try {
            Resource.Success(apiHelper.getUsers())
        } catch (e: Exception) {
            Resource.Error(e.toString())
        }
    }
}
```

然后 `ViewModel` 只根据结果更新界面：

```kotlin
when (val result = getUsersUseCase()) {
    is Resource.Success -> uiState.postValue(UiState.Success(result.data))
    is Resource.Error -> uiState.postValue(UiState.Error(result.message))
}
```

这样做之后，职责边界就清晰了：

- `Repository` 负责和数据源打交道
- `Repository` 负责把异常转换成统一结果
- `ViewModel` 只负责消费结果并驱动 UI

从“防御式写法”转向“结果驱动写法”，是这次重构里我认为非常关键的一步。

这一步之后，项目里的错误处理逻辑开始具备一致性，而不是继续散落在每个页面里各自为战。

---

## 五、第四个改造点：把协程场景放进真正合理的层里

这个项目原本就有很多很适合教学的协程示例，比如：

- 单次网络请求
- 串行网络请求
- 并行网络请求
- 超时控制
- 异常处理
- Supervisor 场景

这次重构没有破坏这些示例的价值，反而让它们的结构更合理了。

以串行请求和并行请求为例，真正适合承载它们的地方，其实不是 `Activity`，也不是堆在 `ViewModel` 里，而是 `UseCase`。

### 串行请求 UseCase

```kotlin
class GetUsersSeriesUseCase(private val userRepository: UserRepository) {

    suspend operator fun invoke(): Resource<List<ApiUser>> {
        val usersResult = userRepository.getUsers()
        if (usersResult is Resource.Error) return usersResult

        val moreUsersResult = userRepository.getMoreUsers()
        if (moreUsersResult is Resource.Error) return moreUsersResult

        return if (usersResult is Resource.Success && moreUsersResult is Resource.Success) {
            val allUsers = mutableListOf<ApiUser>()
            allUsers.addAll(usersResult.data)
            allUsers.addAll(moreUsersResult.data)
            Resource.Success(allUsers)
        } else {
            Resource.Error("Something Went Wrong")
        }
    }
}
```

### 并行请求 UseCase

```kotlin
class GetUsersParallelUseCase(private val userRepository: UserRepository) {

    suspend operator fun invoke(): Resource<List<ApiUser>> = coroutineScope {
        val usersDeferred = async { userRepository.getUsers() }
        val moreUsersDeferred = async { userRepository.getMoreUsers() }

        val usersResult = usersDeferred.await()
        val moreUsersResult = moreUsersDeferred.await()

        if (usersResult is Resource.Success && moreUsersResult is Resource.Success) {
            val allUsers = mutableListOf<ApiUser>()
            allUsers.addAll(usersResult.data)
            allUsers.addAll(moreUsersResult.data)
            Resource.Success(allUsers)
        } else {
            val errorMessage = when {
                usersResult is Resource.Error -> usersResult.message
                moreUsersResult is Resource.Error -> moreUsersResult.message
                else -> "Something Went Wrong"
            }
            Resource.Error(errorMessage)
        }
    }
}
```

这样组织之后，协程本身依然是重点，但代码不会因为“演示协程”而牺牲结构。

这点很重要。

因为真正好的示例项目，不应该只教会别人“怎么 launch / async”，还应该教会别人“这些协程代码应该写在哪一层”。

这也是这次重构最想保留的一点：示例性不变，但工程边界要比原来清楚得多。

---

## 六、这次重构之后，项目结构发生了什么变化

当前项目已经形成了比较清晰的结构：

```text
app/src/main/java/me/amitshekhar/learn/kotlin/coroutines
├── data
│   ├── api
│   ├── local
│   └── repository
├── di
│   └── module
├── domain
│   ├── base
│   ├── repository
│   └── usecase
├── ui
│   ├── base
│   ├── basic
│   ├── errorhandling
│   ├── retrofit
│   ├── room
│   ├── task
│   └── timeout
└── CoroutinesApp.kt
```

如果用一句话概括这个结构变化，那就是：

> 以前是“功能按页面堆起来”，现在是“职责按层次分开”。

再具体一点：

- `data` 层负责网络、本地数据和仓库实现
- `domain` 层负责业务抽象、UseCase 和结果建模
- `ui` 层负责展示和状态消费
- `di` 层负责统一管理依赖

这让整个项目从“示例集合”变成了“具备工程边界的示例项目”。

如果把重构前后做一个非常直接的对比，大概是这样：

| 维度 | 重构前 | 重构后 |
| :--- | :--- | :--- |
| 依赖管理 | 手动 Factory / 页面传递 | `Koin` 统一注入 |
| ViewModel 职责 | 既调数据源，又管异常和状态 | 只协调 UseCase 与 UI 状态 |
| 业务逻辑位置 | 散落在 ViewModel | 下沉到 `UseCase` |
| 异常处理 | 每个页面单独 `try-catch` | `Repository` 统一封装 `Resource` |
| 工程结构 | 更像示例堆叠 | 更接近真实业务分层 |

---

## 七、现在这个项目适合怎么学

如果你是第一次看这个项目，我建议不要一上来就只看协程 API。

更推荐按下面顺序理解：

1. 先看 `ui/basic`，理解最基础的协程用法
2. 再看 `single / series / parallel`，理解不同请求模型
3. 然后看 `domain/usecase`，理解这些协程逻辑为什么被放在这里
4. 再看 `data/repository/UserRepositoryImpl.kt`，理解结果封装和异常处理
5. 最后看 `di/module/AppModule.kt` 和 `CoroutinesApp.kt`，把依赖注入链路串起来

这样你学到的就不只是“协程语法”，而是“协程在 Android 项目里怎么落地”。

如果你是从教程型项目进入业务项目开发，这种视角切换很重要。

---

## 八、这个项目里目前包含哪些示例

首页 `MainActivity` 目前提供了这些入口：

- 基础协程示例：`ui/basic`
- 单次网络请求：`ui/retrofit/single`
- 串行网络请求：`ui/retrofit/series`
- 并行网络请求：`ui/retrofit/parallel`
- Room 数据库读取：`ui/room`
- 超时控制：`ui/timeout`
- `try-catch` 异常处理：`ui/errorhandling/trycatch`
- `CoroutineExceptionHandler`：`ui/errorhandling/exceptionhandler`
- `SupervisorJob` / 错误隔离：`ui/errorhandling/supervisor`
- 单个长任务：`ui/task/onetask`
- 两个长任务：`ui/task/twotasks`

---

## 九、技术栈与运行环境

技术栈：

- Kotlin
- Kotlin Coroutines
- Android Jetpack ViewModel
- LiveData
- Retrofit
- Room
- Koin
- RecyclerView
- Glide
- JUnit4
- Mockito

当前 `app/build.gradle` 中的关键环境配置：

- `compileSdk 36`
- `targetSdk 35`
- `minSdk 24`
- `Java 21`
- `jvmTarget 21`

---

## 十、最后总结

这次优化对我来说，重点不在于“把代码写得更花哨”，而在于把原本偏传统的协程示例，重构成一个更符合真实 Android 项目开发方式的结构。

回头看，这次改造本质上完成了四件事：

- 把依赖管理从手动组装升级为 `Koin DI`
- 把业务逻辑从 `ViewModel` 下沉到 `UseCase`
- 把数据访问和异常封装收敛到 `Repository`
- 把项目从“协程示例集合”推进为“有清晰边界的工程化示例”

如果你也有一个“能跑，但结构开始变重”的 Android 示例项目，这条优化路径是值得参考的。

因为大多数项目真正难的部分，从来都不是“把功能写出来”，而是“让它在新增需求时不会迅速失控”。

后续如果继续往下演进，这个项目还可以再补：

- 更完整的单元测试
- 更统一的错误模型
- Flow / StateFlow 版本示例
- README 配套架构图和页面截图
