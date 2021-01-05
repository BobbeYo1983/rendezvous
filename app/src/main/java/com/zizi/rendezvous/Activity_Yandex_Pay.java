/*package com.zizi.rendezvous;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Currency;

import ru.yandex.money.android.sdk.Amount;
import ru.yandex.money.android.sdk.Checkout;
import ru.yandex.money.android.sdk.ColorScheme;
import ru.yandex.money.android.sdk.MockConfiguration;
import ru.yandex.money.android.sdk.PaymentMethodType;
import ru.yandex.money.android.sdk.PaymentParameters;
import ru.yandex.money.android.sdk.SavePaymentMethod;
import ru.yandex.money.android.sdk.TestParameters;
import ru.yandex.money.android.sdk.TokenizationResult;
import ru.yandex.money.android.sdk.UiParameters;

public class Activity_Yandex_Pay extends AppCompatActivity implements View.OnClickListener {

    int REQUEST_CODE_TOKENIZE; // результат статуса токена на оплату



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_yandex_pay);

        findViewById(R.id.btn_pay_request).setOnClickListener((View.OnClickListener) this);

    }

    void PayRequest() { // начало оплаты, запрос на получения токена, результат в onActivityResult
        PaymentParameters paymentParameters = new PaymentParameters(
                new Amount(BigDecimal.TEN, Currency.getInstance("RUB")),
                "Заявка на встречу",
                "Заявка на встречу позволит просматривать заявки всех пользователей в выбранном городе.",
                "live_AAAAAAAAAAAAAAAAAAAA",
                "12345",
                SavePaymentMethod.OFF,
                Collections.singleton(PaymentMethodType.BANK_CARD)

        );
        TestParameters testParameters = new TestParameters(true, true,
                new MockConfiguration(false, true, 5, new Amount(BigDecimal.TEN, Currency.getInstance("RUB"))));
        //UiParameters uiParameters = new UiParameters(true, new ColorScheme(Color.rgb(0, 114, 245)));
        UiParameters uiParameters = new UiParameters(true, new ColorScheme(Color.rgb(183, 134, 252)));
        Intent intent = Checkout.createTokenizeIntent(this, paymentParameters, testParameters, uiParameters);
        startActivityForResult(intent, REQUEST_CODE_TOKENIZE);
    }

    @Override
    protected void onActivityResult (int requestCode, int resultCode, Intent intent) { // получение токена на оплату
    super.onActivityResult(requestCode, resultCode, intent);

    if (requestCode == REQUEST_CODE_TOKENIZE) {
        switch (resultCode) {
            case RESULT_OK:
                // successful tokenization
                TokenizationResult result = Checkout.createTokenizationResult(intent);

                break;
            case RESULT_CANCELED:
                // user canceled tokenization

                break;
        }
    }
}

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_pay_request) { // если нажата кнопка Оплатить заявку
            PayRequest();
        }

    }
}

*/