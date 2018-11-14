package icar.a5i4s.com.cashierb.helper;

import android.content.Context;

import com.afollestad.materialdialogs.MaterialDialog;

import icar.a5i4s.com.cashierb.R;

/**
 * Created by light on 2016/11/10.
 */

public class MaterialDialogTool {

    public static MaterialDialog.Builder getErrorMessage(Context context , String error){
        return new MaterialDialog.Builder(context)
                .title(context.getString(R.string.alert))
                .content(error)
                .positiveText(context.getString(R.string.payResultClose));
    }
}
