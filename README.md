# ColorPicker
**Simple ColorPicker widgets for Android.**<br/><br/>
* **Style attributes supports both XML & Java code.**<br/><br/>
* **Interface example**
```
 rectColorPicker.setOnColorPickedListener(new RectColorPicker.OnColorPickedListener() {
            @Override
            public void onColorPicked(RectColorPicker picker, int color) {
                // DO SOMETHING
            }
        });
```
* **RectColorPicker**<br/>
[**demo**](https://github.com/Tianscar/ColorPicker/blob/master/app/src/main/java/com/tianscar/colorpickerdemo/MainActivity.java)<br/><br/>
Supports horizontal & vertical orientation.<br/>
Supports ascending & descending order.<br/><br/>
**Horizontal Ascending**<br/>
<img src="https://github.com/Tianscar/ColorPicker/blob/master/readme_assets/Screenshot_1627696478.png" width="20%" height="20%"></img><br/><br/>
**Horizontal Descending**<br/>
<img src="https://github.com/Tianscar/ColorPicker/blob/master/readme_assets/Screenshot_1627696495.png" width="20%" height="20%"></img><br/><br/>
**Vertical Ascending**<br/>
<img src="https://github.com/Tianscar/ColorPicker/blob/master/readme_assets/Screenshot_1627696501.png" width="20%" height="20%"></img><br/><br/>
**Vertical Descending**<br/>
<img src="https://github.com/Tianscar/ColorPicker/blob/master/readme_assets/Screenshot_1627696507.png" width="20%" height="20%"></img><br/><br/>
* **RoundColorPicker**<br/><br/>
Coming soon.

# To get a Git project into your build (gradle):

* Step 1. Add the JitPack repository to your build file<br/>
Add it in your root build.gradle at the end of repositories:<br/>
```
allprojects {
        repositories {
		...
		maven { url 'https://jitpack.io' }
	}
}
```

* Step 2. Add the dependency:<br/>
```
dependencies {
	...
	implementation 'com.github.tianscar:colorpicker:1.0.0.2'
}
```

# License
[MIT](https://github.com/Tianscar/ColorPicker/blob/master/LICENSE) © Tianscar
