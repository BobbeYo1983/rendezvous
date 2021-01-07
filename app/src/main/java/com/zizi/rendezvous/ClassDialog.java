package com.zizi.rendezvous;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

/**
 * Класс формирования вкплывающего диалогового окна
 */
public class ClassDialog extends AppCompatDialogFragment {

    private String title;
    private String message;
    private String widgetName;
    private ClassGlobalApp classGlobalApp;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        classGlobalApp = (ClassGlobalApp) getActivity().getApplicationContext();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton("ОК", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {


                        // Закрываем окно
                        dialog.cancel();

                        if (widgetName != null && widgetName.equals(Data.ACTIVITY_LOGIN)) {

                            //создаем намерение, что хотим перейти на другую активити
                            Intent intent = new Intent(getContext(), ActivityLogin.class);
                            //Intent intent = new Intent(getContext(), ActivityForTest.class);
                            intent.setFlags( Intent.FLAG_ACTIVITY_CLEAR_TASK //очищаем стек с задачей
                                           |Intent.FLAG_ACTIVITY_NEW_TASK   //хотим создать активити в основной очищенной задаче
                            );

                            startActivity(intent); //переходим на другую активити, то есть фактически входим в приложение
                            widgetName = "";
                        }



                    }
                });
        return builder.create();
    }

    /**
     * Задает заголовок всплывающего окна.
     * @param title текст заголовка окна
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Задает текст сообщения всплывабщего окна
     * @param message текст сообщения
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Устанавливает, куда (активити или фрагмент) нужно перенаправить пользователя после нажатия на кнопку OK.
     * @param widgetName имя активити или фрагмента
     */
    public void setPositiveButtonRedirect (String widgetName) {
        this.widgetName = widgetName;
    }

}
