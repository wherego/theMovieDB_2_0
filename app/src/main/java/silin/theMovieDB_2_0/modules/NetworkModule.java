package silin.theMovieDB_2_0.modules;

import android.app.Application;
import android.content.Context;
import android.support.annotation.NonNull;

import com.facebook.stetho.okhttp3.StethoInterceptor;
import com.novoda.merlin.Merlin;
import com.novoda.merlin.MerlinsBeard;
import com.ryanharter.auto.value.moshi.AutoValueMoshiAdapterFactory;
import com.squareup.moshi.Moshi;
import com.squareup.picasso.Picasso;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import okhttp3.Cache;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.moshi.MoshiConverterFactory;
import rx.schedulers.Schedulers;
import silin.theMovieDB_2_0.BuildConfig;
import silin.theMovieDB_2_0.IEnvironment;

/**
 * Created on 7/4/16: theMovieDB_2_0
 */
@Module
public class NetworkModule {
    @Provides
    @Singleton
    Cache providesCache(@NonNull final Application application) {
        int cacheSize = 10 * 1024 * 1024; // 10 MiB

        return new Cache(application.getCacheDir(), cacheSize);
    }

    @Provides
    @Singleton
    OkHttpClient providesOkHttpClient(@NonNull final IEnvironment environment, @NonNull Cache cache) {
        final HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(environment.getHttpLoggingLevel());

        return new OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .addNetworkInterceptor(new StethoInterceptor())
                .cache(cache).build();
    }

    @Provides
    @Singleton
    Merlin providesMerlin(@NonNull final Context context) {
        return new Merlin.Builder()
                .withConnectableCallbacks()
                .withDisconnectableCallbacks()
                .withBindableCallbacks()
                .withLogging(true)
                .build(context);
    }

    @Provides
    @Singleton
    MerlinsBeard providesMerlinBeard(@NonNull final Context context) {
        return MerlinsBeard.from(context);
    }

    @Provides
    @Singleton
    Picasso providesPicasso(@NonNull final Context context) {
        final Picasso picasso = Picasso.with(context);
        picasso.setIndicatorsEnabled(true);
        picasso.setLoggingEnabled(true);
        return picasso;
    }

    @Provides
    @Singleton
    Moshi providesMoshi() {
        return new Moshi.Builder().add(new AutoValueMoshiAdapterFactory()).build();
    }

    @Provides
    @Singleton
    Retrofit providesRetrofit(@NonNull final Moshi moshi, @NonNull final OkHttpClient okHttpClient) {
        return new Retrofit.Builder()
                .baseUrl(BuildConfig.API_BASE_URL)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .addCallAdapterFactory(RxJavaCallAdapterFactory.createWithScheduler(Schedulers.io()))
                .client(okHttpClient)
                .build();
    }
}
