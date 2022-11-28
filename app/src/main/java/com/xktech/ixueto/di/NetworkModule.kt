package com.xktech.ixueto.di

import com.google.gson.Gson
import com.xktech.ixueto.BuildConfig
import com.xktech.ixueto.data.remote.service.*
import com.xktech.ixueto.repository.UserInfoPreferencesRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    var token: String? = null
    private val gson: Gson = Gson().newBuilder()
        .setLenient()
        .serializeNulls()
        .create()

    @Provides
    @Singleton
    fun provideHttpClient(userInfoPreferencesRepository: UserInfoPreferencesRepository): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(Interceptor {
                var request = it.request().newBuilder()
                if (token.isNullOrEmpty()) {
                    token = userInfoPreferencesRepository.getUserInfoSync().token
                }
                if (!token.isNullOrEmpty()) {
                    request =
                        request.addHeader("Authorization", "Bearer $token")

                }
                it.proceed(request.build())
            })
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("${BuildConfig.REMOTE_DOMAIN}/api/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
//            .addCallAdapterFactory(ApiCallAdapterFactory())
            .build()
    }

    @Provides
    @Singleton
    fun provideProfession(retrofit: Retrofit): ProfessionService {
        return retrofit.create(ProfessionService::class.java)
    }

    @Provides
    @Singleton
    fun provideRegion(retrofit: Retrofit): RegionService {
        return retrofit.create(RegionService::class.java)
    }

    @Provides
    @Singleton
    fun userRegion(retrofit: Retrofit): UserService {
        return retrofit.create(UserService::class.java)
    }

    @Provides
    @Singleton
    fun provideSubject(retrofit: Retrofit): SubjectService {
        return retrofit.create(SubjectService::class.java)
    }

    @Provides
    @Singleton
    fun provideCourse(retrofit: Retrofit): CourseService {
        return retrofit.create(CourseService::class.java)
    }

    @Provides
    @Singleton
    fun provideStudy(retrofit: Retrofit): StudyService {
        return retrofit.create(StudyService::class.java)
    }

    @Provides
    @Singleton
    fun provideExam(retrofit: Retrofit): ExamService {
        return retrofit.create(ExamService::class.java)
    }

    @Provides
    @Singleton
    fun provideAuthentication(retrofit: Retrofit): AuthenticationService {
        return retrofit.create(AuthenticationService::class.java)
    }

    @Provides
    @Singleton
    fun provideVersion(retrofit: Retrofit): VersionService {
        return retrofit.create(VersionService::class.java)
    }

    @Provides
    @Singleton
    fun provideQuiz(retrofit: Retrofit): QuizService {
        return retrofit.create(QuizService::class.java)
    }

    @Provides
    @Singleton
    fun provideFeedback(retrofit: Retrofit): FeedbackService {
        return retrofit.create(FeedbackService::class.java)
    }
}