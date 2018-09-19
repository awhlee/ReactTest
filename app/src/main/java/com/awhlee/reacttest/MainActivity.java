package com.awhlee.reacttest;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import com.jakewharton.rxbinding2.view.RxView;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {
    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    // Create a disposable so that we can stop observing on an observable
    private CompositeDisposable mDisposable = new CompositeDisposable();

    // For test 3
    private int mMaxTaps = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onStart() {
        super.onStart();

//        test1();
//        test2();
        test3();
    }

    private void test1() {
        // add to Composite observable
        // .map() operator is used to turn the note into all uppercase letters
        DisposableObserver<TestItem> observable = getItemObservable()
                // Subscribe on the background thread
                .subscribeOn(Schedulers.io())
                // Handle callbacks on the observer on the main thread
                .observeOn(AndroidSchedulers.mainThread())
                // Process the emitter stream before it hits the observer
                .map(new Function<TestItem, TestItem>() {
                    @Override
                    public TestItem apply(TestItem item) throws Exception {
                        // Making the note to all uppercase
                        item.setSubject(item.getSubject().toUpperCase());
                        return item;
                    }
                })
                .subscribeWith(getItemObserver());

        if (observable != null) {
            mDisposable.add(observable);
        }
    }

    // The TestItem observer that will subscribe to the observable.
    private DisposableObserver<TestItem> getItemObserver() {
        return new DisposableObserver<TestItem>() {

            @Override
            public void onNext(TestItem item) {
                Log.e(LOG_TAG, "Item: " + item.toString());
            }

            @Override
            public void onError(Throwable e) {
                Log.e(LOG_TAG, "onError: " + e.getMessage());
            }

            @Override
            public void onComplete() {
                Log.e(LOG_TAG, "All items are emitted!");
            }
        };
    }

    // The item that emits the objects
    private Observable<TestItem> getItemObservable() {
        // The list of items to emit
        // The way to call static members in a Kotlin class
        final List<TestItem> mItems = TestItem.Companion.prepareTestItems();

        // Create and return an observable that emits the test items
        // There are many ways to create the observable like just, fromArray, range, etc...
        return Observable.create(new ObservableOnSubscribe<TestItem>() {
            // When an observer subscribes to this observable, it will emit its data
            @Override
            public void subscribe(ObservableEmitter<TestItem> emitter) {
                // Emit the items if the emitter was not disposed yet.
                for (TestItem item : mItems) {
                    if (!emitter.isDisposed()) {
                        emitter.onNext(item);
                    }
                }
                if (!emitter.isDisposed()) {
                    emitter.onComplete();
                }
            }
        });
    }

    private void test2() {
        // Another test but using operators on the stream before
        // the values are emitted to the observer
        Integer[] numbers = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20};
//        Observable.fromArray(numbers)
        Observable.range(1, 40)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                // Filter to just even numbers
                .filter(new Predicate<Integer>() {
                    @Override
                    public boolean test(Integer integer) throws Exception {
                        return integer % 2 == 0;
                    }
                })
                // Map the value to its square before the observer gets it.
                .map(new Function<Integer, Integer>() {
                    @Override
                    public Integer apply(Integer integer) throws Exception {
                        return integer * 2;
                    }
                })
                .subscribe(new DisposableObserver<Integer>() {
                    @Override
                    public void onNext(Integer integer) {
                        Log.e(LOG_TAG, "Number: " + integer);
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onComplete() {
                        Log.e(LOG_TAG, "All numbers emitted!");
                    }
                });
    }

    // Debounce and input text field
    private void test3() {
        final TextView txtTapResult = findViewById(R.id.tap_result);
        final TextView txtTapResultMax = findViewById(R.id.tap_result_max_count);
        final Button btnTapArea = findViewById(R.id.layout_tap_area);

        Observer<List<Integer>> observer = new Observer<List<Integer>>() {
            @Override
            public void onSubscribe(Disposable d) {
                mDisposable.add(d);
            }

            @Override
            public void onNext(List<Integer> integers) {
                Log.e(LOG_TAG, "onNext: " + integers.size() + " taps received!");
                if (integers.size() > 0) {
                    mMaxTaps = integers.size() > mMaxTaps ? integers.size() : mMaxTaps;
                    txtTapResult.setText(String.format("Received %d taps in 3 secs", integers.size()));
                    txtTapResultMax.setText(String.format("Maximum of %d taps received in this session", mMaxTaps));
                }
            }

            @Override
            public void onError(Throwable e) {
                Log.e(LOG_TAG, "onError: " + e.getMessage());
            }

            @Override
            public void onComplete() {
                Log.e(LOG_TAG, "onComplete");
            }
        };

        // This creates the observable and then we refine it.
        RxView.clicks(btnTapArea)
                // Convert the tap into an integer
                .map(new Function<Object, Integer>() {
                    @Override
                    public Integer apply(Object o) {
                        return 1;
                    }
                })
                // Turns this into emitting a list of integers every 3 seconds
                .buffer(3, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(observer);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDisposable.clear();
    }
}
