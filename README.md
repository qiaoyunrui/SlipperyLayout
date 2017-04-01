# SlipperyLayout

![minSdkVersion 21](https://img.shields.io/badge/minSdkVersion-15-blue.svg)
[![](https://jitpack.io/v/qiaoyunrui/SlipperyLayout.svg)](https://jitpack.io/#qiaoyunrui/SlipperyLayout)

This is a simple ViewGroup that support sliding to show menu, you can set the sliding direction, left、right、top、bottom.

In addition, it also solved the problem of the sliding conflict, so you can put it on your RecyclerView or in the ListView.

## Preview

<img src="preview/preview_1.gif" width="300"/>

[Download the demo](apk/sample.apk)

## dependencies

Step 1. Add the JitPack repository to your build file

  Add it in your root build.gradle at the end of repositories:
  ```gradle
	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
  ```
Step 2. Add the dependency

  ```gradle
  dependencies {
	       compile 'com.github.qiaoyunrui:SlipperyLayout:v1.0'
  }
  ```

## Usage

Add to your layout xml-file:

```java
<com.juhezi.slipperylayout.SlipperyLayout
        android:id="@+id/sl_test"
        android:layout_width="match_parent"
        android:layout_height="200dp"
        android:layout_margin="8dp"
        app:content="@layout/content"
        app:lock="false"
        app:menu="@layout/menu"
        app:slideGravity="top" />
```

Properties provided:

* content: the layout of content

* menu: the layout of menu

* slideGravity: the sliding direction

* lock: if true,can not be slided.

Control SlipperyLayout opened or closed in the code

```java
SlipperyLayout mSlTest = (SlipperyLayout) findViewById(R.id.sl_test);
Button mBtnOpen = (Button) findViewById(R.id.btn_open);
Button mBtnClose = (Button) findViewById(R.id.btn_close);
mBtnOpen.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        mSlTest.openMenuView();   //opene the menu
    }
});
mBtnClose.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        mSlTest.closeMenuView();  //close the menu
    }
});
```

Methods provided:

* `public void setSlideGravity(@SlideGravity int slideGravity)`,set the sliding direction

* `public boolean isLock()`,whether the lock slide

* `public void setLock(boolean lock)`,set the lock (if set to true, SlipperyLayout cannot slide)

* `public boolean isMenuViewVisible()`,get the visibility of menu view

* `public View getMenuView()`,get the menu view

* `public View getContentView()`,get the content view

* `public void openMenuView()`,open menu

* `public void closeMenuView()`,close menu

* `public void setSlideListener()`，set the listener

* `public void removeSlideListener()`，remove the setted listener

### Methods provided in the SlideListener

* `public void onSliding(View menuView, int dx, int dy)`

* `public void onMenuOpened(View menuView)`

* `public void onMenuClosed(View menuView)`

* `public void onStateChanged(@SlipperyLayout.State int oldState, @SlipperyLayout.State int newState)`

## Attention

SlipperyLayout supports the `Padding` and `Margin`, but does not support `elevation`.

If you met what problem in use process, please create the issue or send an email to me, my email is juhezix@163.com.

[中文 README](README_CN.md)
