package com.auraplay.player.data.repository;

import com.auraplay.player.data.local.TrackDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
public final class MusicRepository_Factory implements Factory<MusicRepository> {
  private final Provider<TrackDao> trackDaoProvider;

  public MusicRepository_Factory(Provider<TrackDao> trackDaoProvider) {
    this.trackDaoProvider = trackDaoProvider;
  }

  @Override
  public MusicRepository get() {
    return newInstance(trackDaoProvider.get());
  }

  public static MusicRepository_Factory create(Provider<TrackDao> trackDaoProvider) {
    return new MusicRepository_Factory(trackDaoProvider);
  }

  public static MusicRepository newInstance(TrackDao trackDao) {
    return new MusicRepository(trackDao);
  }
}
