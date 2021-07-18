package org.ossiaustria.amigo.platform.rest.v1.metrics

import io.micrometer.core.instrument.MeterRegistry
import org.ossiaustria.amigo.platform.domain.services.AccountService
import org.ossiaustria.amigo.platform.domain.services.GroupService
import org.ossiaustria.amigo.platform.domain.services.PersonService
import org.ossiaustria.amigo.platform.domain.services.sendables.CallService
import org.ossiaustria.amigo.platform.domain.services.sendables.MessageService
import org.ossiaustria.amigo.platform.domain.services.sendables.MultimediaService
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.concurrent.atomic.AtomicInteger

@Component
class MetricService(
    val accountService: AccountService,
    val callService: CallService,
    val groupService: GroupService,
    val messageService: MessageService,
    val multimediaService: MultimediaService,
    val personService: PersonService,
    val meterRegistry: MeterRegistry,
) {
    val totalAccounts = meterRegistry.gauge("total_accounts", AtomicInteger(0))
    val totalCalls = meterRegistry.gauge("total_calls", AtomicInteger(0))
    val totalGroups = meterRegistry.gauge("total_groups", AtomicInteger(0))
    val totalMessage = meterRegistry.gauge("total_messages", AtomicInteger(0))
    val totalMultimedia = meterRegistry.gauge("total_multimedias", AtomicInteger(0))
    val totalPersons = meterRegistry.gauge("total_persons", AtomicInteger(0))

    @Scheduled(fixedRateString = "60000", initialDelayString = "0")
    fun updateGauges() {
        totalAccounts.set(accountService.count().toInt())
        totalCalls.set(callService.count().toInt())
        totalGroups.set(groupService.count().toInt())
        totalMessage.set(messageService.count().toInt())
        totalMultimedia.set(multimediaService.count().toInt())
        totalPersons.set(personService.count().toInt())
    }

}


