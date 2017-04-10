# FloatingMusicMenu

一款可用于音乐播放器的悬浮菜单按钮，它是基于 `FloatingActionButton` 上完成，能够联动音乐播放器显示歌曲的进度，设置歌曲的封面和通过封面的旋转来展示播放的状态（停止或者播放）。
除此之外，它可以设置一组按钮作为菜单展示，支持上下左右四个方位显示，更方便的是可以在代码中动态的添加按钮或者移除按钮。

![与音乐播放器的联动](http://upload-images.jianshu.io/upload_images/1514994-8190372cf7d17666.gif?imageMogr2/auto-orient/strip)
![作为菜单的功能](http://upload-images.jianshu.io/upload_images/1514994-83d194a23795d0e2.gif?imageMogr2/auto-orient/strip)

## 引入
```groovy
dependencies {
	compile 'com.cpacm:floatingmusicmenu:1.0.0'
}
```
### 具体使用
可以直接在你的layout布局文件中直接定义
```xml
<com.cpacm.FloatingMusicMenu
	android:id="@+id/fmm"
	android:layout_width="wrap_content"
	android:layout_height="wrap_content"
	android:layout_alignParentBottom="true"
	android:layout_alignParentRight="true"
	android:layout_marginBottom="16dp"
	android:layout_marginRight="16dp"
	app:fmm_button_interval="8dp"
	app:fmm_floating_direction="up">

	<android.support.design.widget.FloatingActionButton
		android:id="@+id/add_fab"
		android:layout_width="wrap_content"
		android:layout_height="wrap_content"
		android:src="@drawable/ic_add"
		app:fabSize="mini" />

	<android.support.design.widget.FloatingActionButton
		android:id="@+id/sub_fab"
		android:layout_width="wrap_content"
		android:layout_height="wrap_content"
		android:src="@drawable/ic_remove"
		app:fabSize="mini" />

</com.cpacm.FloatingMusicMenu>
```

### 自定义attr属性表
| 属性 | 类型 | 说明 |
| --   | --   | --   |
| fmm_progress_color | color | 进度条的颜色值 |
| fmm_progress | float | 当前进度（0-100） |
| fmm_button_interval | dimension | 子按钮之间的距离 |
| fmm_cover | reference | 根按钮的封面图片 |
| fmm_progress_percent | integer | 进度条宽度占按钮的百分比，如3表示为3% |
| fmm_backgroundTint | color |根按钮的背景色 |
| fmm_floating_direction | enum | 展开的方向，up表示向上，down表示向下，left表示向左，right表示向右 |

## 说明

感谢star或fork，若需要了解具体实现，请直接clone本工程，源码拥有丰富的注释说明。
有任何Bug或建议欢迎提issue或pull request，或者直接反馈给我.

License
---

    Copyright 2017 cpacm

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.