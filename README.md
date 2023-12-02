# GoogleMaps_Android_Demo
Google 地图导航应用
这个 Android 应用演示了如何将 Google 地图集成到你的应用中。用户可以查看他们的当前位置，在地图上设置目的地，并可视化两点之间的路径。该应用还处理了位置权限，使用标记表示位置，并使用折线表示导航路径。
功能
地图初始化：该应用使用 Google 地图 API 初始化地图，并在 SupportMapFragment 中显示它。

位置处理：用户可以查看他们的当前位置，并使用 FusedLocationProviderClient 实时更新位置。

标记处理：用户可以通过在地图上单击任何位置来添加标记。该应用还为当前位置和目的地设置了标记。

路径绘制：该应用使用 Google Directions API 获取当前位置和目的地之间的路线信息。然后，在地图上绘制折线以表示路径。

协程：使用 Kotlin 协程处理异步任务，如与 Google Directions API 进行网络请求。

权限处理：该应用检查位置权限，并在未授予时请求它们。它包括一个回调来处理权限请求的结果。

UI 交互：当点击按钮（startButton）时，将触发在当前位置和目的地之间显示路径。

入门指南
先决条件
安装了 Android Studio
拥有 Google 地图 API 密钥（将 MainActivity 中的 API_KEY 替换为你的密钥）

克隆存储库：
git clone https://github.com/zhoushaoye/GoogleMaps_Android_Demo.git

在 Android Studio 中打开项目。
用你的 Google 地图 API 密钥替换 MainActivity.kt 中的 API_KEY。
在 Android 模拟器或设备上构建和运行应用。

使用方法
启动应用后，如果尚未授予位置权限，应用将请求权限。
在地图上单击以添加目的地标记。
单击“开始导航”按钮，可视化当前位置和目的地之间的路径。

许可证
该项目基于 MIT 许可证授权 - 有关详细信息，请参阅 LICENSE 文件。
致谢
Google 地图 API