package com.hnmoma.driftbottle.wxapi;

import com.hnmoma.driftbottle.R;
import com.hnmoma.driftbottle.util.MoMaLog;
import com.tencent.mm.sdk.constants.ConstantsAPI;
import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class WXPayEntryActivity extends Activity implements IWXAPIEventHandler{
	
    private IWXAPI api;
	TextView tv_content;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pay_result_wx);
        tv_content = (TextView) findViewById(R.id.tv_content);
    	api = WXAPIFactory.createWXAPI(this, getResources().getString(R.string.WXAPP_ID));
        api.handleIntent(getIntent(), this);
    }

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
        api.handleIntent(intent, this);
	}

	@Override
	public void onReq(BaseReq req) {
	}

	@Override
	public void onResp(BaseResp resp) {
		MoMaLog.d("onPayFinish: ", "onPayFinish, errCode = " + resp.errCode);

		if (resp.getType() == ConstantsAPI.COMMAND_PAY_BY_WX) {
			
			String result = "";
			switch (resp.errCode) {
			case BaseResp.ErrCode.ERR_OK:
				result = "支付完成，将在10分钟左右到账，如有疑问请点击设置-联系客服";
				break;
			case BaseResp.ErrCode.ERR_USER_CANCEL:
				result = "您取消了支付";
				break;
			case BaseResp.ErrCode.ERR_AUTH_DENIED:
				result = "间隔时间太长了，请重新支付";
				break;
			default:
				result = "请重新支付";
				break;
			}
			tv_content.setText(result);
		}
	}
	
	
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_confirm:
			this.finish();
			break;
		}
	}
	
}