# EasyTagCloudView
自定义标签云,带删除功能和删除动画

仿造Recyclerview的Adapter和ViewHolder设计,方便UI拓展和各种事件事件设置

当然,水平有限,还有很多很多的不足,欢迎指正,不胜感激

Step 1. Add it in your root build.gradle at the end of repositories:

	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
Step 2. Add the dependency

	dependencies {
	        compile 'com.github.tangyanghai:EasyTagCloudView:1.02'
	}