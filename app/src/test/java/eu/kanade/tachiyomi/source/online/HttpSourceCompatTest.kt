package eu.kanade.tachiyomi.source.online

import eu.kanade.tachiyomi.animesource.model.AnimeFilterList
import eu.kanade.tachiyomi.animesource.model.SAnime
import eu.kanade.tachiyomi.animesource.model.SEpisode
import eu.kanade.tachiyomi.animesource.online.AnimeHttpSource
import eu.kanade.tachiyomi.source.model.FilterList
import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.model.SManga
import eu.kanade.tachiyomi.source.model.SMangaUpdate
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import okhttp3.Headers
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.jupiter.api.Test
import rx.Observable
import rx.schedulers.Schedulers
import java.util.concurrent.atomic.AtomicInteger

class HttpSourceCompatTest {

    @Test
    fun `HttpSource defaults chapterListParse to emptyList and serializes concurrent calls per manga`() {
        runBlocking(Dispatchers.Default) {
            val activeCalls = AtomicInteger(0)
            val maxConcurrentCalls = AtomicInteger(0)

            val source = object : HttpSource() {
                override val name = "TestManga"
                override val baseUrl = "https://example.com"
                override val lang = "en"
                override val supportsLatest = false
                override val client: OkHttpClient = OkHttpClient()

                override fun headersBuilder() = Headers.Builder()

                override suspend fun getMangaUpdate(
                    manga: SManga,
                    chapters: List<SChapter>,
                    fetchDetails: Boolean,
                    fetchChapters: Boolean,
                ): SMangaUpdate {
                    val current = activeCalls.incrementAndGet()
                    maxConcurrentCalls.updateAndGet { maxOf(it, current) }
                    delay(40)
                    activeCalls.decrementAndGet()
                    return SMangaUpdate(
                        manga = manga.apply { title = "Updated Title" },
                        chapters = listOf(
                            SChapter.create().apply {
                                url = "/ch-1"
                                name = "Ch 1"
                            },
                        ),
                    )
                }

                fun invokeDefaultChapterParse(response: Response) = chapterListParse(response)
                override fun popularMangaRequest(page: Int): Request = throw UnsupportedOperationException()
                override fun searchMangaRequest(page: Int, query: String, filters: FilterList): Request =
                    throw UnsupportedOperationException()

                override fun latestUpdatesRequest(page: Int): Request = throw UnsupportedOperationException()
            }

            val dummyResponse = Response.Builder()
                .request(Request.Builder().url("https://example.com").build())
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body("".toResponseBody())
                .build()

            source.invokeDefaultChapterParse(dummyResponse).shouldBeEmpty()

            val manga = SManga.create().apply {
                url = "/manga/1"
                title = "Initial"
            }
            val results = listOf(
                async { source.getMangaDetails(manga) },
                async { source.getChapterList(manga) },
                async { source.getMangaDetails(manga) },
            ).awaitAll()

            maxConcurrentCalls.get() shouldBe 1
            (results[0] as SManga).title shouldBe "Updated Title"
            (results[1] as List<*>).size shouldBe 1
        }
    }

    @Test
    fun `AnimeHttpSource defaults episodeListParse to emptyList and serializes concurrent calls per anime`() {
        runBlocking(Dispatchers.Default) {
            val activeCalls = AtomicInteger(0)
            val maxConcurrentCalls = AtomicInteger(0)

            val source = object : AnimeHttpSource() {
                override val name = "TestAnime"
                override val baseUrl = "https://example.com"
                override val lang = "en"
                override val supportsLatest = false
                override val client: OkHttpClient = OkHttpClient()

                override fun headersBuilder() = Headers.Builder()

                @Deprecated("Use the combined suspend API instead")
                override fun fetchAnimeDetails(anime: SAnime): Observable<SAnime> {
                    return Observable.fromCallable {
                        val current = activeCalls.incrementAndGet()
                        maxConcurrentCalls.updateAndGet { maxOf(it, current) }
                        Thread.sleep(40)
                        activeCalls.decrementAndGet()
                        anime.apply { title = "Updated Anime" }
                    }.subscribeOn(Schedulers.io())
                }

                @Deprecated("Use the non-RxJava API instead")
                override fun fetchEpisodeList(anime: SAnime): Observable<List<SEpisode>> {
                    return Observable.fromCallable {
                        val current = activeCalls.incrementAndGet()
                        maxConcurrentCalls.updateAndGet { maxOf(it, current) }
                        Thread.sleep(40)
                        activeCalls.decrementAndGet()
                        listOf(
                            SEpisode.create().apply {
                                url = "/ep-1"
                                name = "Ep 1"
                            },
                        )
                    }.subscribeOn(Schedulers.io())
                }

                fun invokeDefaultEpisodeParse(response: Response) = episodeListParse(response)
                override fun popularAnimeRequest(page: Int): Request = throw UnsupportedOperationException()
                override fun searchAnimeRequest(page: Int, query: String, filters: AnimeFilterList): Request =
                    throw UnsupportedOperationException()

                override fun latestUpdatesRequest(page: Int): Request = throw UnsupportedOperationException()
            }

            val dummyResponse = Response.Builder()
                .request(Request.Builder().url("https://example.com").build())
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body("".toResponseBody())
                .build()

            source.invokeDefaultEpisodeParse(dummyResponse).shouldBeEmpty()

            val anime = SAnime.create().apply {
                url = "/anime/1"
                title = "Initial"
            }
            val details = async { source.getAnimeDetails(anime) }
            val episodes = async { source.getEpisodeList(anime) }
            awaitAll(details, episodes)

            maxConcurrentCalls.get() shouldBe 1
        }
    }
}
