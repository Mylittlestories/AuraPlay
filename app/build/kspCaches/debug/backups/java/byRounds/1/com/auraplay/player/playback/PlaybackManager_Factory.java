package com.auraplay.player.playback;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

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
public final class PlaybackManager_Factory implements Factory<PlaybackManager> {
  @Override
  public PlaybackManager get() {
    return newInstance();
  }

  public static PlaybackManager_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static PlaybackManager newInstance() {
    return new PlaybackManager();
  }

  private static final class InstanceHolder {
    private static final PlaybackManager_Factory INSTANCE = new PlaybackManager_Factory();
  }
}
