package info.smemo.nowordschat.presenter;

import android.support.annotation.NonNull;

import info.smemo.nbaseaction.util.LogHelper;
import info.smemo.nbaseaction.util.StringUtil;
import info.smemo.nowordschat.action.UserAction;
import info.smemo.nowordschat.action.UserInfoAction;
import info.smemo.nowordschat.appaction.ActionInterface;
import info.smemo.nowordschat.appaction.controller.UserController;
import info.smemo.nowordschat.contract.MainContract;

import static com.google.common.base.Preconditions.checkNotNull;
import static info.smemo.nowordschat.app.AppConstant.LOG_TAG;

public class MainPresenter implements MainContract.Presenter {

    private final MainContract.View mView;

    public MainPresenter(@NonNull MainContract.View view) {
        mView = checkNotNull(view, "MainContract.View cannot be null");
        mView.setPresenter(this);
    }


    @Override
    public void start() {
        UserAction.getUser(new UserController.LoadUserProfileSuccessListener() {
            @Override
            public void success() {
                LogHelper.e(LOG_TAG, UserInfoAction.getUserInfo().toString());
                //如果用户昵称为空，把他注册的时候放进去的昵称修改进去
                if (StringUtil.isEmpty(UserInfoAction.getUserInfo().nickname)) {
                    final String tmpNick = UserInfoAction.getTmpNickname(mView.getVApplicationContext());
                    //设置默认需要通过好友验证加好友
                    UserInfoAction.setDefaultAllowType();
                    UserInfoAction.setNickname(tmpNick, new ActionInterface.BaseComplete() {
                        @Override
                        public void success() {
                            UserInfoAction.getUserInfo().setNickname(tmpNick);
                        }

                        @Override
                        public void error(int code, String message) {
                            mView.showToastMessage(message);
                        }
                    });
                }
            }

            @Override
            public void error(int code, String message) {
                UserAction.logout();
                LogHelper.e(LOG_TAG, "get user info error:" + message);
                mView.showToastMessage("用户信息获取异常,请重新登录");
                mView.reLogin();
            }
        });
    }
}
