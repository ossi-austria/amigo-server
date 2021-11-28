package org.ossiaustria.amigo.platform.rest.v1.metrics

import io.micrometer.core.instrument.MeterRegistry
import org.ossiaustria.amigo.platform.domain.services.GroupService
import org.ossiaustria.amigo.platform.domain.services.PersonProfileService
import org.ossiaustria.amigo.platform.domain.services.auth.AuthService
import org.ossiaustria.amigo.platform.domain.services.multimedia.AlbumService
import org.ossiaustria.amigo.platform.domain.services.multimedia.MultimediaService
import org.ossiaustria.amigo.platform.domain.services.sendables.CallService
import org.ossiaustria.amigo.platform.domain.services.sendables.MessageService
import org.ossiaustria.amigo.platform.domain.services.sendables.NfcInfoService
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.concurrent.atomic.AtomicInteger

@Component
class MetricService(
    val authService: AuthService,
    val albumService: AlbumService,
    val callService: CallService,
    val groupService: GroupService,
    val messageService: MessageService,
    val multimediaService: MultimediaService,
    val nfcInfoService: NfcInfoService,
    val personService: PersonProfileService,
    meterRegistry: MeterRegistry,
) {
    val totalAccounts: AtomicInteger = meterRegistry.gauge("amigo_stat_accounts_count", AtomicInteger(0))
    val totalAlbums: AtomicInteger = meterRegistry.gauge("amigo_stat_albums_count", AtomicInteger(0))
    val totalCalls: AtomicInteger = meterRegistry.gauge("amigo_stat_calls_count", AtomicInteger(0))
    val totalGroups: AtomicInteger = meterRegistry.gauge("amigo_stat_groups_count", AtomicInteger(0))
    val totalMessage: AtomicInteger = meterRegistry.gauge("amigo_stat_messages_count", AtomicInteger(0))
    val totalMultimedia: AtomicInteger = meterRegistry.gauge("amigo_stat_multimedias_count", AtomicInteger(0))
    val totalNfcs: AtomicInteger = meterRegistry.gauge("amigo_stat_nfcs_count", AtomicInteger(0))
    val totalPersons: AtomicInteger = meterRegistry.gauge("amigo_stat_persons_count", AtomicInteger(0))

    @Scheduled(fixedRateString = "60000", initialDelayString = "0")
    fun updateGauges() {
        totalAccounts.set(authService.count().toInt())
        totalAlbums.set(albumService.count().toInt())
        totalCalls.set(callService.count().toInt())
        totalGroups.set(groupService.count().toInt())
        totalMessage.set(messageService.count().toInt())
        totalMultimedia.set(multimediaService.count().toInt())
        totalNfcs.set(nfcInfoService.count().toInt())
        totalPersons.set(personService.count().toInt())
    }

}


