package com.github.storytime.service.utils;

import com.github.storytime.model.api.ms.AppUser;
import com.github.storytime.model.pb.jaxb.statement.response.ok.Response.Data.Info.Statements.Statement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.xml.datatype.XMLGregorianCalendar;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static java.time.DayOfWeek.SATURDAY;
import static java.time.DayOfWeek.SUNDAY;
import static java.time.Instant.ofEpochMilli;
import static java.time.Instant.ofEpochSecond;
import static java.time.LocalDateTime.of;
import static java.time.ZoneId.of;
import static java.time.ZonedDateTime.now;
import static java.time.ZonedDateTime.ofInstant;
import static java.time.temporal.TemporalAdjusters.firstDayOfMonth;
import static java.time.temporal.TemporalAdjusters.firstDayOfNextMonth;
import static java.util.Set.of;

@Component
public class DateService {

    private static final Set<DayOfWeek> WEEKEND = of(SATURDAY, SUNDAY);
    private final DateTimeFormatter minfinDateTimeFormatter;
    private final DateTimeFormatter isoDateTimeFormatter;
    private final DateTimeFormatter pbDateTimeFormatter;
    private final DateTimeFormatter zenDateTimeFormatter;

    @Autowired
    public DateService(final DateTimeFormatter minfinDateTimeFormatter,
                       final DateTimeFormatter isoDateTimeFormatter,
                       final DateTimeFormatter zenDateTimeFormatter,
                       final DateTimeFormatter pbDateTimeFormatter) {
        this.minfinDateTimeFormatter = minfinDateTimeFormatter;
        this.isoDateTimeFormatter = isoDateTimeFormatter;
        this.pbDateTimeFormatter = pbDateTimeFormatter;
        this.zenDateTimeFormatter = zenDateTimeFormatter;
    }

    public String toPbFormat(ZonedDateTime zonedDateTime) {
        return pbDateTimeFormatter.format(zonedDateTime);
    }

    public String toMinfinFormat(ZonedDateTime zonedDateTime) {
        return minfinDateTimeFormatter.format(zonedDateTime);
    }

    public ZonedDateTime millisUserDate(Long millis, AppUser u) {
        final Instant instant = ofEpochMilli(millis);
        return ofInstant(instant, of(u.getTimeZone()));
    }

    public ZonedDateTime secToUserDate(Long secs, AppUser u) {
        final Instant instant = ofEpochSecond(secs);
        return ofInstant(instant, of(u.getTimeZone()));
    }

    public String toPbFormat(Long millis, AppUser u) {
        return pbDateTimeFormatter.format(millisUserDate(millis, u));
    }

    public String millisToIsoFormat(Long millis, AppUser u) {
        return isoDateTimeFormatter.format(millisUserDate(millis, u));
    }

    public String secsToIsoFormat(Long secs, AppUser u) {
        return isoDateTimeFormatter.format(secToUserDate(secs, u));
    }

    public String millisToIsoFormat(ZonedDateTime zonedDateTime) {
        return isoDateTimeFormatter.format(zonedDateTime);
    }

    public ZonedDateTime xmlDateTimeToZoned(final XMLGregorianCalendar d, final XMLGregorianCalendar t, final String timeZone) {
        return of(d.getYear(), d.getMonth(), d.getDay(), t.getHour(), t.getMinute(), t.getSecond()).atZone(of(timeZone));
    }

    public Long zenStringToZonedSeconds(final String dateString, final String timeZone) {
        return LocalDate
                .parse(dateString, minfinDateTimeFormatter)
                .atStartOfDay(of(timeZone))
                .toInstant()
                .getEpochSecond();
    }

    public String toZenFormat(final XMLGregorianCalendar d,
                              final XMLGregorianCalendar t,
                              final String tz) {
        return zenDateTimeFormatter.format(xmlDateTimeToZoned(d, t, tz));
    }


    public ZonedDateTime getPrevMouthLastBusiness(final Statement s,
                                                  final String timeZone) {
        final XMLGregorianCalendar trandate = s.getTrandate();
        final ZonedDateTime startDate = getPbStatementZonedDateTime(timeZone, trandate).minusMonths(1);

        ZonedDateTime start = startDate.with(firstDayOfMonth());
        final ZonedDateTime end = startDate.with(firstDayOfNextMonth());
        final List<ZonedDateTime> businessDays = new ArrayList<>();

        while (start.isBefore(end)) {
            if (!WEEKEND.contains(start.getDayOfWeek())) {
                businessDays.add(start);
            }
            start = start.plusDays(1);
        }

        return businessDays.get(businessDays.size() - 1);
    }

    public ZonedDateTime getPbStatementZonedDateTime(String timeZone, XMLGregorianCalendar trandate) {
        return of(trandate.getYear(), trandate.getMonth(), trandate.getDay(), 0, 0, 0)
                .atZone(of(timeZone));
    }

    public long getStartOfMouthInSeconds(int year, int mouth, final AppUser u) {
        return YearMonth.of(year, mouth)
                .atDay(1)
                .atStartOfDay(of(u.getTimeZone()))
                .toInstant()
                .getEpochSecond();
    }

    public long getEndOfMouthInSeconds(int year, int mouth, final AppUser u) {
        return YearMonth.of(year, mouth)
                .atEndOfMonth()
                .atStartOfDay(of(u.getTimeZone()))
                .plusHours(24)
                .toInstant()
                .getEpochSecond();
    }

    public long getUserStarDateInMillis(final AppUser appUser) {
        return now().withZoneSameInstant(of(appUser.getTimeZone())).toInstant().toEpochMilli();
    }
}
