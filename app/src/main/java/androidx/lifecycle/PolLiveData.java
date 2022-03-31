package androidx.lifecycle;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

/**
 * 参考下文实现
 * https://www.jianshu.com/p/e08287ec62cd?utm_campaign=hugo
 */
public class PolLiveData<T> extends MutableLiveData<T> {

    @MainThread
    public void observeUnsticky(@NonNull LifecycleOwner owner, @NonNull Observer<? super T> observer) {
        int startVersion = getVersion();
        System.out.println("PolLiveData#observeUnsticky() called, startV===" + startVersion);
        observe(owner, new CustomObserver(observer, startVersion));
    }

    class CustomObserver implements Observer<T> {
        private final Observer<? super T> mObserver;
        private final int mStartVersion;

        public CustomObserver(Observer<? super T> observer, int startVersion) {
            mObserver = observer;
            mStartVersion = startVersion;
        }

        @Override
        public void onChanged(T t) {
            //此处做拦截操作
            int curV = getVersion();
            System.out.println("CustomObserver#onChanged() called, t===" + t + ", startV=" + mStartVersion + ", version=" + curV);
            if (curV > mStartVersion) {
                System.out.println("notify changed...");
                mObserver.onChanged(t);
            } else {
                System.out.println("ignore changed!!!");
            }
        }
    }

}
