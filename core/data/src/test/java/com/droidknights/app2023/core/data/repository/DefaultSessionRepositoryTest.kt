package com.droidknights.app2023.core.data.repository

import app.cash.turbine.test
import com.droidknights.app2023.core.data.api.fake.FakeGithubRawApi
import com.droidknights.app2023.core.model.Level
import com.droidknights.app2023.core.model.Room
import com.droidknights.app2023.core.model.Session
import com.droidknights.app2023.core.model.Speaker
import com.droidknights.app2023.core.model.Tag
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import kotlinx.datetime.LocalDateTime

internal class DefaultSessionRepositoryTest : StringSpec() {

    init {
        val repository: SessionRepository = DefaultSessionRepository(
            githubRawApi = FakeGithubRawApi()
        )
        "역직렬화 테스트" {
            val expected = listOf(
                Session(
                    id = "1",
                    title = "Keynote",
                    content = listOf(),
                    speakers = listOf(),
                    level = Level.ETC,
                    tags = listOf(Tag("키노트")),
                    room = Room.ETC,
                    startTime = LocalDateTime(2023, 9, 12, 13, 0),
                    endTime = LocalDateTime(2023, 9, 12, 13, 20),
                ),
                Session(
                    id = "2",
                    title = "Jetpack Compose로 Android UI 개발하기",
                    content = listOf("Jetpack Compose는 네이티브 UI를 빌드하기 위한 Android의 최신 권장 도구 키트입니다."),
                    speakers = listOf(
                        Speaker(
                            name = "김컴포즈",
                            imageUrl = "https://developer.android.com/static/courses/android-basics-compose/images/hero-assets/unit-logo.svg",
                        )
                    ),
                    level = Level.ADVANCED,
                    tags = listOf(
                        Tag("Jetpack"),
                        Tag("UI"),
                    ),
                    room = Room.TRACK1,
                    startTime = LocalDateTime(2023, 9, 12, 13, 25),
                    endTime = LocalDateTime(2023, 9, 12, 13, 55),
                ),
            )
            val actual = repository.getSessions()
            actual shouldBe expected
        }

        "북마크 추가 테스트" {
            repository.getBookmarkedSessionIds().test {
                awaitItem() shouldBe emptySet()

                repository.bookmarkSession(sessionId = "1", bookmark = true)
                awaitItem() shouldBe setOf("1")

                repository.bookmarkSession(sessionId = "2", bookmark = true)
                awaitItem() shouldBe setOf("1", "2")
            }
        }

        "북마크 제거 테스트" {
            // given : [1, 2, 3]
            val bookmarkedSessionIds = listOf("1", "2", "3")
            bookmarkedSessionIds.forEach {
                repository.bookmarkSession(it, true)
            }

            repository.getBookmarkedSessionIds().test {
                awaitItem() shouldBe setOf("1", "2", "3")

                // [1, 2, 3] -> [1, 3]
                repository.bookmarkSession(sessionId = "2", bookmark = false)
                awaitItem() shouldBe setOf("1", "3")

                // [1, 3] -> [1]
                repository.bookmarkSession(sessionId = "3", bookmark = false)
                awaitItem() shouldBe setOf("1")
            }
        }
    }
}
