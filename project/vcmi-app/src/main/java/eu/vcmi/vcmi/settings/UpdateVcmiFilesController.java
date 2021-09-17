package eu.vcmi.vcmi.settings;

import androidx.appcompat.app.AppCompatActivity;
import android.view.View;

import eu.vcmi.vcmi.R;
import eu.vcmi.vcmi.util.GeneratedVersion;

/**
 * @author nullkiller
 */
public class UpdateVcmiFilesController extends LauncherSettingController<Void, Void>
{
    private View.OnClickListener mOnSelectedAction;

    public UpdateVcmiFilesController(
            final AppCompatActivity act,
            final View.OnClickListener onSelectedAction)
    {
        super(act);
        mOnSelectedAction = onSelectedAction;
    }

    @Override
    protected String mainText()
    {
        return mActivity.getString(R.string.launcher_btn_import_zip);
    }

    @Override
    protected String subText()
    {
        return mActivity.getString(R.string.launcher_btn_import_zip_description);
    }

    @Override
    public void onClick(final View v)
    {
        mOnSelectedAction.onClick(v);
    }
}
