package org.libsdl.app;

import android.content.Context;
import android.graphics.Rect;
import androidx.appcompat.widget.LinearLayoutCompat;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.widget.LinearLayout;

import eu.vcmi.vcmi.R;
import eu.vcmi.vcmi.util.Log;

class DummyEdit extends LinearLayout
{
    final DummyEditText mEditText;

    public DummyEdit(Context context)
    {
        super(context);

        mEditText = new DummyEditText(context);
        setBackgroundResource(R.drawable.overlay_edittext_background);
        mEditText.setLayoutParams(new LinearLayoutCompat.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        addView(mEditText);
    }

    @Override
    public boolean requestFocus(final int direction, final Rect previouslyFocusedRect)
    {
        Log.d(this, "Requesting focus");
        return mEditText.requestFocus(direction, previouslyFocusedRect);
    }

    public void notifyContentChanged(final String textContext)
    {
        mEditText.setText(textContext);
    }

    static class DummyEditText extends androidx.appcompat.widget.AppCompatEditText implements View.OnKeyListener
    {
        InputConnection mInputConnection;

        public DummyEditText(final Context context)
        {
            super(context);
            setOnKeyListener(this);
        }

        @Override
        public boolean onCheckIsTextEditor()
        {
            return false;
        }

        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event)
        {
            Log.d(this, "xx# " + keyCode + "; " + event.getAction());
            // This handles the hardware keyboard input
            if (event.isPrintingKey() || keyCode == KeyEvent.KEYCODE_SPACE)
            {
                if (event.getAction() == KeyEvent.ACTION_DOWN)
                {
                    mInputConnection.commitText(String.valueOf((char) event.getUnicodeChar()), 1);
                }
                return false;
            }

            return SDLActivity.mHolder.surface().onKey(v, keyCode, event);
        }

        //
        @Override
        public boolean onKeyPreIme(int keyCode, KeyEvent event)
        {
            Log.d(this, "xx#pre " + keyCode + "; " + event.getAction());
            // As seen on StackOverflow: http://stackoverflow.com/questions/7634346/keyboard-hide-event
            // FIXME: Discussion at http://bugzilla.libsdl.org/show_bug.cgi?id=1639
            // FIXME: This is not a 100% effective solution to the problem of detecting if the keyboard is showing or not
            // FIXME: A more effective solution would be to assume our Layout to be RelativeLayout or LinearLayout
            // FIXME: And determine the keyboard presence doing this: http://stackoverflow.com/questions/2150078/how-to-check-visibility-of-software-keyboard-in-android
            // FIXME: An even more effective way would be if Android provided this out of the box, but where would the fun be in that :)
            if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK)
            {
                final View edit = SDLActivity.mHolder.edit();
                if (edit != null && edit.getVisibility() == View.VISIBLE)
                {
                    SDLActivity.onNativeKeyboardFocusLost();
                }
            }
            return super.onKeyPreIme(keyCode, event);
        }

        @Override
        public InputConnection onCreateInputConnection(EditorInfo outAttrs)
        {
            mInputConnection = new SDLInputConnection(this, true);

            outAttrs.inputType = InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD;
            outAttrs.imeOptions = EditorInfo.IME_FLAG_NO_EXTRACT_UI
                                  | 33554432 /* API 11: EditorInfo.IME_FLAG_NO_FULLSCREEN */;

            return mInputConnection;
        }
    }
}
