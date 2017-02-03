package info.smemo.nbaseaction.http;

import android.support.annotation.NonNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import info.smemo.nbaseaction.util.LogHelper;
import info.smemo.nbaseaction.util.StringUtil;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.exceptions.Exceptions;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.Response;

public class DownloadUtil extends HttpUtil {

    public static void download(@NonNull String url, @NonNull String path, @NonNull String fileName, @NonNull HttpDownloadListener listener) {
        download(url, path, fileName, true, listener);
    }

    public static void download(@NonNull final String url, @NonNull final String path, @NonNull final String fileName, @NonNull final boolean cover, @NonNull final HttpDownloadListener listener) {
        Observable.create(new ObservableOnSubscribe<Request>() {

            @Override
            public void subscribe(ObservableEmitter<Request> subscriber) throws Exception {
                //url can't be empty
                if (StringUtil.isEmpty(url)) {
                    listener.failure("Http url is empty");
                    return;
                }
                //get http url and check
                HttpUrl requestUrl = HttpUrl.parse(url);
                if (null == requestUrl) {
                    listener.failure("HttpUrl[" + url + "] has error");
                    return;
                }
                LogHelper.i(TAG_HTTP, "network:Download Http Request Url:" + url);

                Request request = new Request.Builder().url(url).build();

                subscriber.onNext(request);
                subscriber.onComplete();
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .map(new Function<Request, File>() {
                    @Override
                    public File apply(Request request) throws Exception {
                        Response response;
                        try {

                            response = client.newCall(request).execute();
                            if (null == response) {
                                throw Exceptions.propagate(new Throwable("response is null"));
                            }
                            if (!response.isSuccessful()) {
                                throw Exceptions.propagate(new Throwable("response.is not Successful " + response.code() + ":" + response.toString()));
                            }
                            long contentLength = response.body().contentLength();

                            listener.start(contentLength);

                            InputStream is = response.body().byteStream();
                            int len;
                            long size = 0;
                            byte[] buf = new byte[1024 * 8];
                            File fileDir = new File(path);
                            if (!fileDir.exists()) {
                                fileDir.mkdirs();
                            }
                            File file = new File(fileDir, fileName);
                            if (file.exists()) {
                                //如果文件存在,切大小相同,直接成功
                                if (!cover && file.length() == contentLength) {
                                    listener.downloading(contentLength, file.length(), 100f, true);
                                    return file;
                                } else {
                                    file.delete();
                                }
                            }

                            listener.downloading(contentLength, size, 0f, false);

                            try {
                                FileOutputStream outputStream = new FileOutputStream(file);
                                while ((len = is.read(buf)) != -1) {
                                    size += len;
                                    outputStream.write(buf, 0, len);
                                    listener.downloading(contentLength, size, ((100l * (float) size) / (float) contentLength), false);

                                }
                                outputStream.flush();
                                outputStream.close();
                                is.close();
                            } catch (FileNotFoundException e) {
                                throw Exceptions.propagate(e);
                            } catch (IOException e) {

                                throw Exceptions.propagate(e);
                            }

                            listener.downloading(contentLength, size, 100f, true);

                            return file;
                        } catch (Exception e) {
                            throw Exceptions.propagate(e);
                        }
                    }
                })
                .onErrorReturn(new Function<Throwable, File>() {

                    @Override
                    public File apply(Throwable throwable) throws Exception {
                        throwable.printStackTrace();
                        listener.failure(throwable.getMessage());
                        return null;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(new Consumer<File>() {

                    @Override
                    public void accept(File file) throws Exception {
                        if (file == null || !file.exists()) {
                            listener.failure("on next : file is null");
                        } else {
                            listener.success(file);
                        }
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .doOnComplete(new Action() {
                    @Override
                    public void run() throws Exception {
                        LogHelper.i(TAG_HTTP, "Http Download Request[" + url + "] Completed");
                    }
                })
                .doOnError(new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        throwable.printStackTrace();
                        listener.failure(throwable.getMessage());
                    }
                })
                .subscribe();
    }

    public interface HttpDownloadListener {

        void success(@NonNull File file);

        void failure(String message);

        void start(long contentLength);

        void downloading(long contentLength, long downsize, float progress, boolean done);
    }
}
