package com.vpaliy.espressoinaction.presentation.mvp.presenter;

import android.support.annotation.NonNull;

import com.vpaliy.espressoinaction.common.scheduler.BaseSchedulerProvider;
import com.vpaliy.espressoinaction.di.scope.ViewScope;
import com.vpaliy.espressoinaction.domain.IRepository;
import com.vpaliy.espressoinaction.domain.model.Order;
import com.vpaliy.espressoinaction.presentation.mvp.contract.OrdersContract.Presenter;
import com.vpaliy.espressoinaction.presentation.mvp.contract.OrdersContract.View;

import javax.inject.Inject;

import rx.subscriptions.CompositeSubscription;

@ViewScope
public class OrdersPresenter implements Presenter {

    private IRepository<Order> repository;
    private BaseSchedulerProvider schedulerProvider;
    private CompositeSubscription subscription;

    private View view;

    @Inject
    public OrdersPresenter(IRepository<Order> iRepository,
                           BaseSchedulerProvider schedulerProvider){
        this.repository=iRepository;
        this.schedulerProvider=schedulerProvider;
    }

    @Override
    public void attachView(@NonNull View view) {
        this.view=view;
    }

    @Override
    public void stop() {

    }

    @Override
    public void start() {

    }

    @Override
    public void onCoffeeSelected(int id) {

    }
}
