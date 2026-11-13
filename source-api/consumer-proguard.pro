# Types manga extensions implement, extend or construct
-keep class eu.kanade.tachiyomi.source.MangaSource { public protected *; }
-keep class eu.kanade.tachiyomi.source.MangaSource$DefaultImpls { public protected *; }
-keep class eu.kanade.tachiyomi.source.CatalogueSource { public protected *; }
-keep class eu.kanade.tachiyomi.source.CatalogueSource$DefaultImpls { public protected *; }
-keep class eu.kanade.tachiyomi.source.ConfigurableSource { public protected *; }
-keep class eu.kanade.tachiyomi.source.SourceFactory { public protected *; }
-keep class eu.kanade.tachiyomi.source.UnmeteredSource { public protected *; }
-keep class eu.kanade.tachiyomi.source.model.** { public protected *; }
-keep class eu.kanade.tachiyomi.source.online.** { public protected *; }
-keep class eu.kanade.tachiyomi.source.** extends eu.kanade.tachiyomi.source.MangaSource { public protected *; }

# Types anime extensions implement, extend or construct
-keep class eu.kanade.tachiyomi.animesource.AnimeSource { public protected *; }
-keep class eu.kanade.tachiyomi.animesource.AnimeSource$DefaultImpls { public protected *; }
-keep class eu.kanade.tachiyomi.animesource.AnimeCatalogueSource { public protected *; }
-keep class eu.kanade.tachiyomi.animesource.AnimeCatalogueSource$DefaultImpls { public protected *; }
-keep class eu.kanade.tachiyomi.animesource.ConfigurableAnimeSource { public protected *; }
-keep class eu.kanade.tachiyomi.animesource.AnimeSourceFactory { public protected *; }
-keep class eu.kanade.tachiyomi.animesource.UnmeteredSource { public protected *; }
-keep class eu.kanade.tachiyomi.animesource.model.** { public protected *; }
-keep class eu.kanade.tachiyomi.animesource.online.** { public protected *; }
-keep class eu.kanade.tachiyomi.animesource.** extends eu.kanade.tachiyomi.animesource.AnimeSource { public protected *; }

-keep,allowoptimization class eu.kanade.tachiyomi.util.JsoupExtensionsKt { public protected *; }
