package org.komapper.extension.validator

import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.FunSpec
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset

class TemporalValidatorTest :
    FunSpec({

        context("LocalDate") {
            context("future") {
                val date = LocalDate.of(2025, 1, 1)
                val zone = ZoneOffset.UTC
                val instant = date.atStartOfDay(zone).toInstant()
                val clock = Clock.fixed(instant, zone)
                val validator = Kova.localDate(clock).future()

                test("success") {
                    validator.tryValidate(date.plusDays(1)).shouldBeRight()
                }

                test("failure - present") {
                    validator.tryValidate(date).shouldBeLeft()
                }

                test("failure - past") {
                    validator.tryValidate(date.minusDays(1)).shouldBeLeft()
                }
            }

            context("futureOrPresent") {
                val date = LocalDate.of(2025, 1, 1)
                val zone = ZoneOffset.UTC
                val instant = date.atStartOfDay(zone).toInstant()
                val clock = Clock.fixed(instant, zone)
                val validator = Kova.localDate(clock).futureOrPresent()

                test("success - future") {
                    validator.tryValidate(date.plusDays(1)).shouldBeRight()
                }

                test("success - present") {
                    validator.tryValidate(date).shouldBeRight()
                }

                test("failure - past") {
                    validator.tryValidate(date.minusDays(1)).shouldBeLeft()
                }
            }

            context("past") {
                val date = LocalDate.of(2025, 1, 1)
                val zone = ZoneOffset.UTC
                val instant = date.atStartOfDay(zone).toInstant()
                val clock = Clock.fixed(instant, zone)
                val validator = Kova.localDate(clock).past()

                test("success") {
                    validator.tryValidate(date.minusDays(1)).shouldBeRight()
                }

                test("failure - present") {
                    validator.tryValidate(date).shouldBeLeft()
                }

                test("failure - future") {
                    validator.tryValidate(date.plusDays(1)).shouldBeLeft()
                }
            }

            context("pastOrPresent") {
                val date = LocalDate.of(2025, 1, 1)
                val zone = ZoneOffset.UTC
                val instant = date.atStartOfDay(zone).toInstant()
                val clock = Clock.fixed(instant, zone)
                val validator = Kova.localDate(clock).pastOrPresent()

                test("success - past") {
                    validator.tryValidate(date.minusDays(1)).shouldBeRight()
                }

                test("success - present") {
                    validator.tryValidate(date).shouldBeRight()
                }

                test("failure - future") {
                    validator.tryValidate(date.plusDays(1)).shouldBeLeft()
                }
            }

            context("min") {
                val minDate = LocalDate.of(2025, 1, 1)
                val validator = Kova.localDate().min(minDate)

                test("success - equal") {
                    validator.tryValidate(minDate).shouldBeRight()
                }

                test("success - greater") {
                    validator.tryValidate(minDate.plusDays(1)).shouldBeRight()
                }

                test("failure") {
                    validator.tryValidate(minDate.minusDays(1)).shouldBeLeft()
                }
            }

            context("max") {
                val maxDate = LocalDate.of(2025, 12, 31)
                val validator = Kova.localDate().max(maxDate)

                test("success - equal") {
                    validator.tryValidate(maxDate).shouldBeRight()
                }

                test("success - less") {
                    validator.tryValidate(maxDate.minusDays(1)).shouldBeRight()
                }

                test("failure") {
                    validator.tryValidate(maxDate.plusDays(1)).shouldBeLeft()
                }
            }

            context("gt") {
                val date = LocalDate.of(2025, 6, 15)
                val validator = Kova.localDate().gt(date)

                test("success") {
                    validator.tryValidate(date.plusDays(1)).shouldBeRight()
                }

                test("failure - equal") {
                    validator.tryValidate(date).shouldBeLeft()
                }

                test("failure - less") {
                    validator.tryValidate(date.minusDays(1)).shouldBeLeft()
                }
            }

            context("gte") {
                val date = LocalDate.of(2025, 6, 15)
                val validator = Kova.localDate().gte(date)

                test("success - greater") {
                    validator.tryValidate(date.plusDays(1)).shouldBeRight()
                }

                test("success - equal") {
                    validator.tryValidate(date).shouldBeRight()
                }

                test("failure") {
                    validator.tryValidate(date.minusDays(1)).shouldBeLeft()
                }
            }

            context("lt") {
                val date = LocalDate.of(2025, 6, 15)
                val validator = Kova.localDate().lt(date)

                test("success") {
                    validator.tryValidate(date.minusDays(1)).shouldBeRight()
                }

                test("failure - equal") {
                    validator.tryValidate(date).shouldBeLeft()
                }

                test("failure - greater") {
                    validator.tryValidate(date.plusDays(1)).shouldBeLeft()
                }
            }

            context("lte") {
                val date = LocalDate.of(2025, 6, 15)
                val validator = Kova.localDate().lte(date)

                test("success - less") {
                    validator.tryValidate(date.minusDays(1)).shouldBeRight()
                }

                test("success - equal") {
                    validator.tryValidate(date).shouldBeRight()
                }

                test("failure") {
                    validator.tryValidate(date.plusDays(1)).shouldBeLeft()
                }
            }
        }

        context("LocalTime") {
            context("future") {
                val date = LocalDate.of(2025, 1, 1)
                val time = LocalTime.of(12, 0, 0)
                val zone = ZoneOffset.UTC
                val instant = date.atTime(time).toInstant(zone)
                val clock = Clock.fixed(instant, zone)
                val validator = Kova.localTime(clock).future()

                test("success") {
                    validator.tryValidate(time.plusHours(1)).shouldBeRight()
                }

                test("failure - present") {
                    validator.tryValidate(time).shouldBeLeft()
                }

                test("failure - past") {
                    validator.tryValidate(time.minusHours(1)).shouldBeLeft()
                }
            }

            context("futureOrPresent") {
                val date = LocalDate.of(2025, 1, 1)
                val time = LocalTime.of(12, 0, 0)
                val zone = ZoneOffset.UTC
                val instant = date.atTime(time).toInstant(zone)
                val clock = Clock.fixed(instant, zone)
                val validator = Kova.localTime(clock).futureOrPresent()

                test("success - future") {
                    validator.tryValidate(time.plusHours(1)).shouldBeRight()
                }

                test("success - present") {
                    validator.tryValidate(time).shouldBeRight()
                }

                test("failure - past") {
                    validator.tryValidate(time.minusHours(1)).shouldBeLeft()
                }
            }

            context("past") {
                val date = LocalDate.of(2025, 1, 1)
                val time = LocalTime.of(12, 0, 0)
                val zone = ZoneOffset.UTC
                val instant = date.atTime(time).toInstant(zone)
                val clock = Clock.fixed(instant, zone)
                val validator = Kova.localTime(clock).past()

                test("success") {
                    validator.tryValidate(time.minusHours(1)).shouldBeRight()
                }

                test("failure - present") {
                    validator.tryValidate(time).shouldBeLeft()
                }

                test("failure - future") {
                    validator.tryValidate(time.plusHours(1)).shouldBeLeft()
                }
            }

            context("pastOrPresent") {
                val date = LocalDate.of(2025, 1, 1)
                val time = LocalTime.of(12, 0, 0)
                val zone = ZoneOffset.UTC
                val instant = date.atTime(time).toInstant(zone)
                val clock = Clock.fixed(instant, zone)
                val validator = Kova.localTime(clock).pastOrPresent()

                test("success - past") {
                    validator.tryValidate(time.minusHours(1)).shouldBeRight()
                }

                test("success - present") {
                    validator.tryValidate(time).shouldBeRight()
                }

                test("failure - future") {
                    validator.tryValidate(time.plusHours(1)).shouldBeLeft()
                }
            }

            context("min") {
                val minTime = LocalTime.of(9, 0, 0)
                val validator = Kova.localTime().min(minTime)

                test("success - equal") {
                    validator.tryValidate(minTime).shouldBeRight()
                }

                test("success - greater") {
                    validator.tryValidate(minTime.plusHours(1)).shouldBeRight()
                }

                test("failure") {
                    validator.tryValidate(minTime.minusHours(1)).shouldBeLeft()
                }
            }

            context("max") {
                val maxTime = LocalTime.of(17, 0, 0)
                val validator = Kova.localTime().max(maxTime)

                test("success - equal") {
                    validator.tryValidate(maxTime).shouldBeRight()
                }

                test("success - less") {
                    validator.tryValidate(maxTime.minusHours(1)).shouldBeRight()
                }

                test("failure") {
                    validator.tryValidate(maxTime.plusHours(1)).shouldBeLeft()
                }
            }

            context("gt") {
                val time = LocalTime.of(12, 0, 0)
                val validator = Kova.localTime().gt(time)

                test("success") {
                    validator.tryValidate(time.plusHours(1)).shouldBeRight()
                }

                test("failure - equal") {
                    validator.tryValidate(time).shouldBeLeft()
                }

                test("failure - less") {
                    validator.tryValidate(time.minusHours(1)).shouldBeLeft()
                }
            }

            context("gte") {
                val time = LocalTime.of(12, 0, 0)
                val validator = Kova.localTime().gte(time)

                test("success - greater") {
                    validator.tryValidate(time.plusHours(1)).shouldBeRight()
                }

                test("success - equal") {
                    validator.tryValidate(time).shouldBeRight()
                }

                test("failure") {
                    validator.tryValidate(time.minusHours(1)).shouldBeLeft()
                }
            }

            context("lt") {
                val time = LocalTime.of(12, 0, 0)
                val validator = Kova.localTime().lt(time)

                test("success") {
                    validator.tryValidate(time.minusHours(1)).shouldBeRight()
                }

                test("failure - equal") {
                    validator.tryValidate(time).shouldBeLeft()
                }

                test("failure - greater") {
                    validator.tryValidate(time.plusHours(1)).shouldBeLeft()
                }
            }

            context("lte") {
                val time = LocalTime.of(12, 0, 0)
                val validator = Kova.localTime().lte(time)

                test("success - less") {
                    validator.tryValidate(time.minusHours(1)).shouldBeRight()
                }

                test("success - equal") {
                    validator.tryValidate(time).shouldBeRight()
                }

                test("failure") {
                    validator.tryValidate(time.plusHours(1)).shouldBeLeft()
                }
            }
        }

        context("LocalDateTime") {
            context("future") {
                val dateTime = LocalDateTime.of(2025, 1, 1, 12, 0, 0)
                val zone = ZoneOffset.UTC
                val instant = dateTime.toInstant(zone)
                val clock = Clock.fixed(instant, zone)
                val validator = Kova.localDateTime(clock).future()

                test("success") {
                    validator.tryValidate(dateTime.plusHours(1)).shouldBeRight()
                }

                test("failure - present") {
                    validator.tryValidate(dateTime).shouldBeLeft()
                }

                test("failure - past") {
                    validator.tryValidate(dateTime.minusHours(1)).shouldBeLeft()
                }
            }

            context("futureOrPresent") {
                val dateTime = LocalDateTime.of(2025, 1, 1, 12, 0, 0)
                val zone = ZoneOffset.UTC
                val instant = dateTime.toInstant(zone)
                val clock = Clock.fixed(instant, zone)
                val validator = Kova.localDateTime(clock).futureOrPresent()

                test("success - future") {
                    validator.tryValidate(dateTime.plusHours(1)).shouldBeRight()
                }

                test("success - present") {
                    validator.tryValidate(dateTime).shouldBeRight()
                }

                test("failure - past") {
                    validator.tryValidate(dateTime.minusHours(1)).shouldBeLeft()
                }
            }

            context("past") {
                val dateTime = LocalDateTime.of(2025, 1, 1, 12, 0, 0)
                val zone = ZoneOffset.UTC
                val instant = dateTime.toInstant(zone)
                val clock = Clock.fixed(instant, zone)
                val validator = Kova.localDateTime(clock).past()

                test("success") {
                    validator.tryValidate(dateTime.minusHours(1)).shouldBeRight()
                }

                test("failure - present") {
                    validator.tryValidate(dateTime).shouldBeLeft()
                }

                test("failure - future") {
                    validator.tryValidate(dateTime.plusHours(1)).shouldBeLeft()
                }
            }

            context("pastOrPresent") {
                val dateTime = LocalDateTime.of(2025, 1, 1, 12, 0, 0)
                val zone = ZoneOffset.UTC
                val instant = dateTime.toInstant(zone)
                val clock = Clock.fixed(instant, zone)
                val validator = Kova.localDateTime(clock).pastOrPresent()

                test("success - past") {
                    validator.tryValidate(dateTime.minusHours(1)).shouldBeRight()
                }

                test("success - present") {
                    validator.tryValidate(dateTime).shouldBeRight()
                }

                test("failure - future") {
                    validator.tryValidate(dateTime.plusHours(1)).shouldBeLeft()
                }
            }

            context("min") {
                val minDateTime = LocalDateTime.of(2025, 1, 1, 12, 0, 0)
                val validator = Kova.localDateTime().min(minDateTime)

                test("success - equal") {
                    validator.tryValidate(minDateTime).shouldBeRight()
                }

                test("success - greater") {
                    validator.tryValidate(minDateTime.plusHours(1)).shouldBeRight()
                }

                test("failure") {
                    validator.tryValidate(minDateTime.minusHours(1)).shouldBeLeft()
                }
            }

            context("max") {
                val maxDateTime = LocalDateTime.of(2025, 12, 31, 23, 59, 59)
                val validator = Kova.localDateTime().max(maxDateTime)

                test("success - equal") {
                    validator.tryValidate(maxDateTime).shouldBeRight()
                }

                test("success - less") {
                    validator.tryValidate(maxDateTime.minusHours(1)).shouldBeRight()
                }

                test("failure") {
                    validator.tryValidate(maxDateTime.plusHours(1)).shouldBeLeft()
                }
            }

            context("gt") {
                val dateTime = LocalDateTime.of(2025, 6, 15, 12, 0, 0)
                val validator = Kova.localDateTime().gt(dateTime)

                test("success") {
                    validator.tryValidate(dateTime.plusHours(1)).shouldBeRight()
                }

                test("failure - equal") {
                    validator.tryValidate(dateTime).shouldBeLeft()
                }

                test("failure - less") {
                    validator.tryValidate(dateTime.minusHours(1)).shouldBeLeft()
                }
            }

            context("gte") {
                val dateTime = LocalDateTime.of(2025, 6, 15, 12, 0, 0)
                val validator = Kova.localDateTime().gte(dateTime)

                test("success - greater") {
                    validator.tryValidate(dateTime.plusHours(1)).shouldBeRight()
                }

                test("success - equal") {
                    validator.tryValidate(dateTime).shouldBeRight()
                }

                test("failure") {
                    validator.tryValidate(dateTime.minusHours(1)).shouldBeLeft()
                }
            }

            context("lt") {
                val dateTime = LocalDateTime.of(2025, 6, 15, 12, 0, 0)
                val validator = Kova.localDateTime().lt(dateTime)

                test("success") {
                    validator.tryValidate(dateTime.minusHours(1)).shouldBeRight()
                }

                test("failure - equal") {
                    validator.tryValidate(dateTime).shouldBeLeft()
                }

                test("failure - greater") {
                    validator.tryValidate(dateTime.plusHours(1)).shouldBeLeft()
                }
            }

            context("lte") {
                val dateTime = LocalDateTime.of(2025, 6, 15, 12, 0, 0)
                val validator = Kova.localDateTime().lte(dateTime)

                test("success - less") {
                    validator.tryValidate(dateTime.minusHours(1)).shouldBeRight()
                }

                test("success - equal") {
                    validator.tryValidate(dateTime).shouldBeRight()
                }

                test("failure") {
                    validator.tryValidate(dateTime.plusHours(1)).shouldBeLeft()
                }
            }
        }
    })
