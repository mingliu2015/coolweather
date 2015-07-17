package reciever;

import service.AutoUpdateService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.sax.StartElementListener;

public class AutoUpdateReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Intent it = new Intent(context, AutoUpdateService.class);
		context.startService(it);
	}

}
