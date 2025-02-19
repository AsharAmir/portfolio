package ee.tenman.portfolio.service

import ee.tenman.portfolio.configuration.RedisConfiguration.Companion.INSTRUMENT_CACHE
import ee.tenman.portfolio.domain.Instrument
import ee.tenman.portfolio.repository.InstrumentRepository
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.cache.annotation.Caching
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class InstrumentService(private val instrumentRepository: InstrumentRepository) {

  @Transactional(readOnly = true)
  @Cacheable(value = [INSTRUMENT_CACHE], key = "#id")
  fun getInstrumentById(id: Long): Instrument = instrumentRepository.findById(id)
    .orElseThrow { RuntimeException("Instrument not found with id: $id") }

  @Transactional
  @Caching(
    evict = [
      CacheEvict(value = [INSTRUMENT_CACHE], key = "#instrument.id", condition = "#instrument.id != null"),
      CacheEvict(value = [INSTRUMENT_CACHE], key = "#instrument.symbol"),
      CacheEvict(value = [INSTRUMENT_CACHE], key = "'allInstruments'")
    ]
  )
  fun saveInstrument(instrument: Instrument): Instrument = instrumentRepository.save(instrument)

  @Transactional
  @Caching(
    evict = [
      CacheEvict(value = [INSTRUMENT_CACHE], key = "#id"),
      CacheEvict(value = [INSTRUMENT_CACHE], key = "'allInstruments'")
    ]
  )
  fun deleteInstrument(id: Long) = instrumentRepository.deleteById(id)

  @Transactional(readOnly = true)
  @Cacheable(value = [INSTRUMENT_CACHE], key = "'allInstruments'", unless = "#result.isEmpty()")
  fun getAllInstruments(): List<Instrument> {
    return instrumentRepository.findAll()
  }

}
