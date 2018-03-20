package com.mengmengda.yqreader.activity;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.mengmengda.yqreader.R;
import com.mengmengda.yqreader.adapter.ReaderPagerAdapter;
import com.mengmengda.yqreader.api.ApiUtil;
import com.mengmengda.yqreader.been.C;
import com.mengmengda.yqreader.been.UpdateApp;
import com.mengmengda.yqreader.been.User;
import com.mengmengda.yqreader.common.ReaderApplication;
import com.mengmengda.yqreader.common.UpdateManager;
import com.mengmengda.yqreader.db.dao.UserDbUtil;
import com.mengmengda.yqreader.fragment.FragmentChoice;
import com.mengmengda.yqreader.fragment.FragmentCollection;
import com.mengmengda.yqreader.fragment.FragmentDiscover;
import com.mengmengda.yqreader.logic.BookHistoryUtil;
import com.mengmengda.yqreader.logic.MyParam;
import com.mengmengda.yqreader.util.AppManager;
import com.mengmengda.yqreader.util.CommonUtil;
import com.mengmengda.yqreader.util.DisplayUtil;
import com.mengmengda.yqreader.util.GlideUtil;
import com.mengmengda.yqreader.util.LogUtils;
import com.mengmengda.yqreader.util.SharePreferenceUtils;
import com.mengmengda.yqreader.util.UI;
import com.mengmengda.yqreader.widget.CustomFontTextView;
import com.mengmengda.yqreader.widget.ReaderDialog;
import com.mengmengda.yqreader.widget.ReaderViewPager;
import com.mengmengda.yqreader.widget.ShapedImageView;
import com.minggo.pluto.logic.LogicManager;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.minggo.pluto.logic.LogicManager.LogicManagerType.GET__MODEL__ONLY_NETWORK;

/**
 * Created by liqisi on 2018/2/1.
 */

public class IndexActivity extends BaseActivity {

	@BindView(R.id.vp_index)
	ReaderViewPager viewPager;
	@BindView(R.id.tl_TabLayout)
	TabLayout tl_TabLayout;
	@BindView(R.id.abl_head)
	public LinearLayout abl_head;

	public FragmentChoice mFragmentChoice;
	public FragmentCollection mFragmentCollection;
	public FragmentDiscover mFragmentDiscover;
	@BindView(R.id.index_drawer)
	public DrawerLayout indexDrawer;
	@BindView(R.id.rl_userVip)
	RelativeLayout rlUserVip;
	@BindView(R.id.rl_history)
	RelativeLayout rlHistory;
	@BindView(R.id.rl_about)
	RelativeLayout rlAbout;
	@BindView(R.id.user_vip_renew)
	TextView userVipRenew;
	@BindView(R.id.tv_switchAccount)
	TextView tvSwitchAccount;
	@BindView(R.id.iv_userHead)
	ShapedImageView ivUserHead;
	@BindView(R.id.tv_userName)
	CustomFontTextView tvUserName;
	@BindView(R.id.tv_history_count)
	TextView tvHistoryCount;
	@BindView(R.id.tv_userVip)
	TextView tvUserVip;
	@BindView(R.id.tv_vip_deadline)
	TextView tvVipDeadline;
	@BindView(R.id.tv_userInfo)
	RelativeLayout tv_userInfo;
	private List<Fragment> fragments = new ArrayList<>();

	private int[] titleIds = new int[]{R.string.index_choice, R.string.index_collection,
			R.string.index_discover};
	private int[] tabIcons = {R.drawable.selector_index_choice, R.drawable.selector_index_collection,
			R.drawable.selector_index_discover};

	public User user;
	private Gson gson;
	private int curVersionCode;
	private ReaderDialog readerDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_index);
		DisplayUtil.setStatusBarFullTranslucent(this);
		ButterKnife.bind(this);
		initUI();
		// 这是master的修改
		// 这是branch2的修改
		// 这是master的新增
		requestData();
		SharePreferenceUtils.putString(this, SharePreferenceUtils.USER_CONFING, SharePreferenceUtils.USER_CONFING_ADVANCE_READ, C.CONSTANTS_READ_LOADING_ONE + "");
		indexDrawer.setDrawerListener(new DrawerLayout.SimpleDrawerListener() {
			@Override
			public void onDrawerOpened(View drawerView) {
				super.onDrawerOpened(drawerView);
				requestData();
			}

			@Override
			public void onDrawerClosed(View drawerView) {
				super.onDrawerClosed(drawerView);
			}
		});

	}

	private void requestData() {
		//检查书架是否有更新
//		requestBookCollectUpdateCount();
		//版本更新
		checkNewVersion();
		// 刷新用户数据
		gson = new Gson();
		user = UserDbUtil.getCurrentUser(this);
		requestUserData();
	}

	/**
	 * 检测新版本
	 */
	private void checkNewVersion() {
		try {
			PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), 0);
			curVersionCode = info.versionCode;
			LogicManager logicManager = new LogicManager(mUiHandler, UpdateApp.class, LogicManager.LogicManagerType.GET__MODEL__ONLY_NETWORK);
			logicManager
					.setParamClass(MyParam.NewVersionParam.class)
					.setParam(ApiUtil.addRequiredParam())
					.setParam("versionCode", curVersionCode)
					.execute();
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace(System.err);
		}
	}

	private void requestUserData() {
		User user = UserDbUtil.getCurrentUser(this);
		LogUtils.info("user:" + user);
		if (user != null) {
			LogicManager logicManager = new LogicManager(mUiHandler, com.minggo.pluto.model.Result.class, GET__MODEL__ONLY_NETWORK);
			logicManager
					.setParamClass(MyParam.LoginParam.class)
					.setParam(ApiUtil.addRequiredParam())
					.setParam("encryptId", user.encryptId)
					.execute();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		refreshUserUI();
		if (C.USER_REFRESH) {
			requestUserData();
			C.USER_REFRESH = false;
		}
	}

	@Override
	public void handleUiMessage(Message msg) {
		switch (msg.what) {
			case MyParam.LoginParam.WHAT:
				if (msg.obj != null) {
					com.minggo.pluto.model.Result result = (com.minggo.pluto.model.Result) msg.obj;
					User user = gson.fromJson(gson.toJson(result.content), User.class);
					SharePreferenceUtils.putString(this, SharePreferenceUtils.USER_CONFING, SharePreferenceUtils.USER_ENCRYPT_UID, user.encryptId);
					UserDbUtil.saveOrUpdateUser(this, user);
					refreshUserUI();
				}
				break;
			case MyParam.NewVersionParam.WHAT:
				final UpdateApp mUpdateApp = (UpdateApp) msg.obj;
				if (mUpdateApp != null) {
					if (curVersionCode < mUpdateApp.getVersionCode()) {
						ReaderApplication.APK_READ_INSTALL = true;
						ReaderApplication.APK_READ_INSTALL_URL = mUpdateApp.getDownloadUrl();
						ReaderApplication.APK_READ_INSTALL_MSG = mUpdateApp.getUpdateLog();
						ReaderApplication.APK_READ_INSTALL_CODE = mUpdateApp.getVersionCode();
						if (readerDialog != null) {
							readerDialog.dismiss();
							readerDialog.cancel();
						}

						final UpdateManager updateManager = UpdateManager.getUpdateManager();
						updateManager.checkApp(IndexActivity.this, tl_TabLayout);
						// http://192.168.0.82:8080/solr/apk/123.apk
						updateManager.setApkUrl(ReaderApplication.APK_READ_INSTALL_URL,
								ReaderApplication.APK_READ_INSTALL_MSG, ReaderApplication.APK_READ_INSTALL_CODE, true);

						readerDialog = new ReaderDialog(IndexActivity.this, R.style.readerDialog,
								ReaderDialog.DIALOG_TEXT_TWO_BUTTON,
								IndexActivity.this.getString(R.string.version_update),
								ReaderApplication.APK_READ_INSTALL_MSG, new ReaderDialog.OnDialogClickListener() {
							@Override
							public void onDialogClick(int state) {
								if (state == ReaderDialog.ONCLICK_LEFT) {
									readerDialog.dismiss();
									readerDialog.cancel();
									if (mUpdateApp.getIs_coerce() == 1) { // 如果本次更新是强制，取消则退出应用
										IndexActivity.super.onBackPressed();
										AppManager.getAppManager().App_Exit();
									}
								} else if (state == ReaderDialog.ONCLICK_RIGHT) {
									readerDialog.dismiss();
									readerDialog.cancel();
									updateManager.showDownloadDialog();
								}
							}
						});
						readerDialog.show();
					}
				}
				break;
		}
	}

	private void refreshUserUI() {
		user = UserDbUtil.getCurrentUser(this);
		int bookCount = 0;
		if (user != null) {
			tvUserName.setText(user.nickName);
			if (BookHistoryUtil.GetList() != null) {
				bookCount = BookHistoryUtil.GetList().size();
			}
			tvHistoryCount.setText(getString(R.string.user_have_read_count, bookCount + ""));
			if (!TextUtils.isEmpty(user.payMonthlyEndTime) && !user.payMonthlyEndTime.equals("0")) {
				tvUserVip.setBackgroundResource(R.drawable.shape_user_tip_bg);
				userVipRenew.setText(R.string.user_vip_renew);
				tvVipDeadline.setText(user.payMonthlyEndTime);
			} else {
				tvUserVip.setBackgroundResource(R.drawable.shape_user_vip);
				userVipRenew.setText(R.string.user_vip_month);
				tvVipDeadline.setText(R.string.user_not_vip);
			}
			GlideUtil.loadImg(this, user.avatar, R.mipmap.nav_head, ivUserHead);
		} else {
			tvHistoryCount.setText(getString(R.string.user_have_read_count, bookCount + ""));
		}
	}

	/**
	 * 初始化UI
	 */
	private void initUI() {
		int leftPadding = DisplayUtil.dip2px(this, 16);
		int bottomPadding = DisplayUtil.dip2px(this, 20);
		int topPadding = DisplayUtil.dip2px(this, 32);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			tv_userInfo.setPadding(leftPadding, DisplayUtil.dip2px(this, 50), leftPadding, bottomPadding);
		} else {
			tv_userInfo.setPadding(leftPadding, topPadding, leftPadding, bottomPadding);
		}

		mFragmentChoice = new FragmentChoice();
		mFragmentCollection = new FragmentCollection();
		mFragmentDiscover = new FragmentDiscover();

		fragments.add(mFragmentChoice);
		fragments.add(mFragmentCollection);
		fragments.add(mFragmentDiscover);

		ReaderPagerAdapter pagerAdapter = new ReaderPagerAdapter(this, this.getSupportFragmentManager(), fragments, titleIds);
		viewPager.setScrollable(false);
		viewPager.setAdapter(pagerAdapter);
		tl_TabLayout.setupWithViewPager(viewPager);
		viewPager.setOffscreenPageLimit(fragments.size());

		int normalColor = ContextCompat.getColor(this, R.color.common_secondary_font3);
		int selectColor = ContextCompat.getColor(this, R.color.colorAccent);
		int[] colors = new int[]{
				selectColor,
				selectColor,
				normalColor
		};
		int[][] states = new int[colors.length][];
		states[0] = new int[]{android.R.attr.state_selected};
		states[1] = new int[]{android.R.attr.state_pressed};
		states[2] = new int[]{};
		ColorStateList colorList = new ColorStateList(states, colors);

		for (int i = 0; i < tl_TabLayout.getTabCount(); i++) {
			TabLayout.Tab tab = tl_TabLayout.getTabAt(i);
			View view = getLayoutInflater().inflate(R.layout.item_tab_layout, null);
			((ImageView) view.findViewById(R.id.iv_TabIcon)).setImageResource(tabIcons[i]);

			TextView tv_TabText = (TextView) view.findViewById(R.id.tv_TabText);
			tv_TabText.setTextColor(colorList);
			tv_TabText.setText(titleIds[i]);

//			tabLayoutDotMap.put(fragmentList.get(i), view.findViewById(R.id.iv_prompt_dot));
			if (i == 0) {//setCustomView TabLayout初始首选项不会被选中
				view.setSelected(true);
			}
			if (tab != null) {
				tab.setCustomView(view);
			}
		}

		tl_TabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
			@Override
			public void onTabSelected(TabLayout.Tab tab) {
				//去除ViewPager切换动画
				viewPager.setCurrentItem(tab.getPosition(), false);
			}

			@Override
			public void onTabUnselected(TabLayout.Tab tab) {
			}

			@Override
			public void onTabReselected(TabLayout.Tab tab) {

			}
		});

	}

	public void setBottomMenuState(boolean isShow) {
		if (isShow) {
			UI.visible(abl_head);
		} else {
			UI.gone(abl_head);
		}
	}

	@OnClick({R.id.rl_history, R.id.rl_about, R.id.user_vip_renew,
			R.id.tv_switchAccount, R.id.tv_userInfo})
	public void onViewClicked(View view) {
		switch (view.getId()) {
			case R.id.rl_history:
				CommonUtil.startActivity(this, HistoryReadRecordActivity.class);
				break;
			case R.id.rl_about:
				CommonUtil.startActivity(this, AboutActivity.class);
				break;
			case R.id.user_vip_renew:
				CommonUtil.startActivity(this, RechargeActivity.class);
				mobClickAgentEvent(C.RECHARGE_MEMBER_CLICK);
				break;
			case R.id.tv_switchAccount:
				CommonUtil.startActivity(this, LoginActivity.class);
				break;
			case R.id.tv_userInfo:
				CommonUtil.startActivity(this, LoginActivity.class);
				break;
		}
		indexDrawer.postDelayed(new Runnable() {
			@Override
			public void run() {
				indexDrawer.closeDrawer(Gravity.LEFT);
			}
		}, 500);

	}

	private long currentTime;
	@Override
	public void onBackPressed() {
		if(System.currentTimeMillis()<currentTime){
			super.onBackPressed();
			AppManager.getAppManager().App_Exit();
		}else{
			currentTime=System.currentTimeMillis()+2000;
			showToast(R.string.app_exit_tip);
		}

	}
}
