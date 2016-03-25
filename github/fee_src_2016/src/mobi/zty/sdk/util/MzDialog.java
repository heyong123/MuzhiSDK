package mobi.zty.sdk.util;

import mobi.zty.pay.R;
import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;

public class MzDialog extends Dialog{

	public MzDialog(Context context) {
		this(context,0);
	}
	
	public MzDialog(Context context,int theme) {
		super(context,theme);
		initDialog(context);
	}

	private void initDialog(Context context) {
		
		setContentView(R.layout.dialog);
//		ImageView ima = (ImageView) findViewById(R.id.dialog_img);
//		ima.setOnClickListener(new View.OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				dismiss();
//				
//			}
//		});
		
	}

	

}
