package com.auraplay.player.di;

import com.auraplay.player.data.local.AuraPlayDatabase;
import com.auraplay.player.data.local.TrackDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava"
})
public final class AppModule_ProvideTrackDaoFactory implements Factory<TrackDao> {
  private final Provider<AuraPlayDatabase> dbProvider;

  public AppModule_ProvideTrackDaoFactory(Provider<AuraPlayDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public TrackDao get() {
    return provideTrackDao(dbProvider.get());
  }

  public static AppModule_ProvideTrackDaoFactory create(Provider<AuraPlayDatabase> dbProvider) {
    return new AppModule_ProvideTrackDaoFactory(dbProvider);
  }

  public static TrackDao provideTrackDao(AuraPlayDatabase db) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideTrackDao(db));
  }
}
