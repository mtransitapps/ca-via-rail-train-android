package org.mtransit.parser.ca_via_rail_train;

import static org.mtransit.commons.RegexUtils.BEGINNING;
import static org.mtransit.commons.RegexUtils.DIGIT_CAR;
import static org.mtransit.commons.RegexUtils.END;
import static org.mtransit.commons.RegexUtils.WHITESPACE_CAR;
import static org.mtransit.commons.RegexUtils.group;
import static org.mtransit.commons.RegexUtils.mGroup;
import static org.mtransit.commons.RegexUtils.oneOrMore;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mtransit.commons.CleanUtils;
import org.mtransit.commons.Cleaner;
import org.mtransit.parser.DefaultAgencyTools;
import org.mtransit.parser.MTLog;
import org.mtransit.parser.gtfs.data.GRoute;
import org.mtransit.parser.mt.data.MAgency;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

// https://www.viarail.ca/en/developer-resources
// https://www.viarail.ca/fr/ressources-developpeurs
// TODO Real-Time (custom) https://tsimobile.viarail.ca/data/allData.json
public class VIARailTrainAgencyTools extends DefaultAgencyTools {

	public static void main(@NotNull String[] args) {
		new VIARailTrainAgencyTools().start(args);
	}

	@Nullable
	@Override
	public List<Locale> getSupportedLanguages() {
		return LANG_EN;
	}

	@Override
	public boolean defaultExcludeEnabled() {
		return true;
	}

	@NotNull
	@Override
	public String getAgencyName() {
		return "VIA Rail";
	}

	@NotNull
	@Override
	public Integer getAgencyRouteType() {
		return MAgency.ROUTE_TYPE_TRAIN;
	}

	@Override
	public boolean defaultAgencyColorEnabled() {
		return true;
	}

	private static final String AGENCY_COLOR = "#FFCB06"; // YELLOW (from GTFS)

	@NotNull
	@Override
	public String getAgencyColor() {
		return AGENCY_COLOR;
	}

	@Override
	public boolean defaultRouteIdEnabled() {
		return true;
	}

	@Override
	public @Nullable String getRouteIdCleanupRegex() {
		return "-"; // no guarantee it doesn't merge routes by mistake // TODO need replace with
	}

	private static final Cleaner ROUTE_ID_CLEAN = new Cleaner(
			group(BEGINNING + group(oneOrMore(DIGIT_CAR)) + "-" + group(oneOrMore(DIGIT_CAR)) + END),
			mGroup(2) + "000" + mGroup(3)
	);

	@Override
	public long getRouteId(@NotNull GRoute gRoute) {
		//noinspection deprecation
		String routeIdS = gRoute.getRouteId();
		routeIdS = ROUTE_ID_CLEAN.clean(routeIdS);
		return Long.parseLong(routeIdS);
	}

	private static final Cleaner VIA_RAIL_ = new Cleaner("(via rail)", true);

	// from GTFS stops code
	private static final List<Cleaner> RTS_CITIES = Arrays.asList(
			new Cleaner("(montr(é|e)al)", "MTRL", true),
			new Cleaner("(toronto)", "TRTO", true),
			new Cleaner("(ottawa)", "OTTW", true),
			new Cleaner("(qu(e|é)bec)", "QBEC", true),
			new Cleaner("(kingston)", "KGON", true),
			new Cleaner("(london)", "LNDN", true),
			new Cleaner("(windsor)", "WDON", true),
			new Cleaner("(vancouver)", "VCVR", true),
			new Cleaner("(jasper)", "JASP", true),
			new Cleaner("(prince rupert)", "PRUP", true),
			new Cleaner("(new York)", "NEWY", true),
			new Cleaner("(niagara falls)", "NIAG", true),
			new Cleaner("(sarnia)", "SARN", true),
			new Cleaner("(the pas)", "TPAS", true),
			new Cleaner("(churchill)", "CHUR", true),
			new Cleaner("(sudbury)", "SUDB", true),
			new Cleaner("(white river)", "WHTR", true),
			new Cleaner("(fallowfield)", "FALL", true),
			new Cleaner("(jonquière)", "JONQ", true),
			new Cleaner("(senneterre)", "SENN", true),
			new Cleaner("(halifax)", "HLFX", true),
			new Cleaner("(winnipeg)", "WNPG", true)
	);

	private static final Cleaner _DASH_ = new Cleaner(oneOrMore(WHITESPACE_CAR) + "-" + oneOrMore(WHITESPACE_CAR), "-");

	@Override
	public @NotNull String getRouteShortName(@NotNull GRoute gRoute) {
		String routeShortName = gRoute.getRouteShortName();
		routeShortName = VIA_RAIL_.clean(routeShortName);
		if (routeShortName.isEmpty()) {
			routeShortName = gRoute.getRouteLongNameOrDefault();
			for (Cleaner cityCleaner : RTS_CITIES) {
				routeShortName = cityCleaner.clean(routeShortName);
			}
			routeShortName = _DASH_.clean(routeShortName);
			if (routeShortName.length() > 9) {
				throw new MTLog.Fatal("Unexpected route short name '%s' for %s!", routeShortName, gRoute.toStringPlus());
			}
		}
		return routeShortName;
	}

	@Override
	public boolean useRouteShortNameForRouteId() {
		return false; // don't make sense for humans
	}

	@Override
	public boolean defaultRouteLongNameEnabled() {
		return true;
	}

	@NotNull
	@Override
	public String cleanRouteLongName(@NotNull String routeLongName) {
		routeLongName = CleanUtils.SAINT.matcher(routeLongName).replaceAll(CleanUtils.SAINT_REPLACEMENT);
		return CleanUtils.cleanLabel(routeLongName);
	}

	@NotNull
	@Override
	public String cleanTripHeadsign(@NotNull String tripHeading) {
		return CleanUtils.cleanLabelFR(tripHeading);
	}

	@Override
	public boolean directionFinderEnabled() {
		return true;
	}

	@NotNull
	@Override
	public String cleanStopName(@NotNull String gStopName) {
		gStopName = CleanUtils.cleanBounds(Locale.FRENCH, gStopName);
		gStopName = CleanUtils.cleanStreetTypes(gStopName);
		return CleanUtils.cleanLabelFR(gStopName);
	}
}
