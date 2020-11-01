package com.example.carcontrollermqtt.service;

import java.util.HashMap;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

public class MessagesRxBus {
    private static volatile MessagesRxBus instance;
    private final Subject<Object> subject;

    private HashMap<String, CompositeDisposable> subscriptionMap;

    public MessagesRxBus() {
        subject = PublishSubject.create().toSerialized();
    }

    public static MessagesRxBus getInstance() {
        if (instance == null) {
            synchronized (MessagesRxBus.class) {
                if (instance == null) {
                    instance = new MessagesRxBus();
                }
            }
        }

        return instance;
    }

    public void post(Object o) {
        subject.onNext(o);
    }

    /**
     * Returns a Flowable instance with backpressure of the specified type
     *
     * @param <T>
     * @param type
     * @return
     */
    public <T> Flowable<T> getFlowable(Class<T> type) {
        return subject.toFlowable(BackpressureStrategy.BUFFER)
                .ofType(type);
    }

    /**
     * A default subscription method
     *
     * @param <T>
     * @param type
     * @param next
     * @param error
     * @return
     */
    public <T> Disposable subscribe(Class<T> type, Consumer<T> next, Consumer<Throwable> error) {
        return getFlowable(type)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(next, error);
    }

    /**
     * Is there an observer subscription?
     *
     * @return
     */
    public boolean hasObservers() {
        return subject.hasObservers();
    }

    /**
     * disposable after saving subscription
     *
     * @param o
     * @param disposable
     */
    public void addSubscription(Object o, Disposable disposable) {
        if (subscriptionMap == null) {
            subscriptionMap = new HashMap<>();
        }
        String key = o.getClass().getName();
        if (subscriptionMap.get(key) != null) {
            subscriptionMap.get(key).add(disposable);
        } else {
            //One-time containers can hold multiple containers and provide additions and removals.
            CompositeDisposable disposables = new CompositeDisposable();
            disposables.add(disposable);
            subscriptionMap.put(key, disposables);
        }
    }

    /**
     * unsubscribe
     *
     * @param o
     */
    public void unSubscribe(Object o) {
        if (subscriptionMap == null) {
            return;
        }

        String key = o.getClass().getName();
        if (!subscriptionMap.containsKey(key)) {
            return;
        }
        if (subscriptionMap.get(key) != null) {
            subscriptionMap.get(key).dispose();
        }

        subscriptionMap.remove(key);
    }
}
