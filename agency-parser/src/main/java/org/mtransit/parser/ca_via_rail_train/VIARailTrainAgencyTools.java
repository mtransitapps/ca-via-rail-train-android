package org.mtransit.parser.ca_via_rail_train;

import static org.mtransit.commons.RegexUtils.BEGINNING;
import static org.mtransit.commons.RegexUtils.DIGIT_CAR;
import static org.mtransit.commons.RegexUtils.END;
import static org.mtransit.commons.RegexUtils.group;
import static org.mtransit.commons.RegexUtils.mGroup;
import static org.mtransit.commons.RegexUtils.oneOrMore;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mtransit.commons.Cleaner;
import org.mtransit.parser.DefaultAgencyTools;
import org.mtransit.parser.gtfs.data.GRoute;

public class VIARailTrainAgencyTools extends DefaultAgencyTools {

	public static void main(@NotNull String[] args) {
		new VIARailTrainAgencyTools().start(args);
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
		//noinspection DiscouragedApi
		return Long.parseLong(ROUTE_ID_CLEAN.clean(gRoute.getOriginalRouteId()));
	}
}
