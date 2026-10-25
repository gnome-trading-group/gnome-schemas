package group.gnometrading.schemas.converters;

import group.gnometrading.schemas.RecordType;
import group.gnometrading.schemas.converters.mbp1.Mbp1ToBbo1mBulkConverter;
import group.gnometrading.schemas.converters.mbp1.Mbp1ToBbo1mConverter;
import group.gnometrading.schemas.converters.mbp1.Mbp1ToBbo1sBulkConverter;
import group.gnometrading.schemas.converters.mbp1.Mbp1ToBbo1sConverter;
import group.gnometrading.schemas.converters.mbp1.Mbp1ToTradesBulkConverter;
import group.gnometrading.schemas.converters.mbp1.Mbp1ToTradesConverter;
import group.gnometrading.schemas.converters.mbp10.Mbp10ToMbp1BulkConverter;
import group.gnometrading.schemas.converters.mbp10.Mbp10ToMbp1Converter;
import group.gnometrading.schemas.converters.trades.TradesToOhlcv1hBulkConverter;
import group.gnometrading.schemas.converters.trades.TradesToOhlcv1hConverter;
import group.gnometrading.schemas.converters.trades.TradesToOhlcv1mBulkConverter;
import group.gnometrading.schemas.converters.trades.TradesToOhlcv1mConverter;
import group.gnometrading.schemas.converters.trades.TradesToOhlcv1sBulkConverter;
import group.gnometrading.schemas.converters.trades.TradesToOhlcv1sConverter;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public final class SchemaConversionRegistry {

    private SchemaConversionRegistry() {}

    private static final Map<RecordType, Map<RecordType, Supplier<SchemaConverter<?, ?>>>> converters = new HashMap<>();
    private static final Map<RecordType, Map<RecordType, Supplier<SchemaBulkConverter<?, ?>>>> bulkConverters =
            new HashMap<>();

    static {
        // Every record type gets an entry, so adding one can never leave a lookup without a map.
        for (var type : RecordType.values()) {
            converters.put(type, new HashMap<>());
            bulkConverters.put(type, new HashMap<>());
        }

        // MBP10 converters
        converters.get(RecordType.MBP_10).put(RecordType.MBP_1, Mbp10ToMbp1Converter::new);
        converters
                .get(RecordType.MBP_10)
                .put(
                        RecordType.TRADES,
                        () -> new WaterfallConverter<>(new Mbp10ToMbp1Converter(), new Mbp1ToTradesConverter()));
        converters
                .get(RecordType.MBP_10)
                .put(
                        RecordType.BBO_1S,
                        () -> new WaterfallConverter<>(new Mbp10ToMbp1Converter(), new Mbp1ToBbo1sConverter()));
        converters
                .get(RecordType.MBP_10)
                .put(
                        RecordType.BBO_1M,
                        () -> new WaterfallConverter<>(new Mbp10ToMbp1Converter(), new Mbp1ToBbo1mConverter()));
        converters
                .get(RecordType.MBP_10)
                .put(
                        RecordType.OHLCV_1S,
                        () -> new WaterfallConverter<>(
                                new Mbp10ToMbp1Converter(),
                                new Mbp1ToTradesConverter(),
                                new TradesToOhlcv1sConverter()));
        converters
                .get(RecordType.MBP_10)
                .put(
                        RecordType.OHLCV_1M,
                        () -> new WaterfallConverter<>(
                                new Mbp10ToMbp1Converter(),
                                new Mbp1ToTradesConverter(),
                                new TradesToOhlcv1mConverter()));
        converters
                .get(RecordType.MBP_10)
                .put(
                        RecordType.OHLCV_1H,
                        () -> new WaterfallConverter<>(
                                new Mbp10ToMbp1Converter(),
                                new Mbp1ToTradesConverter(),
                                new TradesToOhlcv1hConverter()));
        bulkConverters.get(RecordType.MBP_10).put(RecordType.MBP_1, Mbp10ToMbp1BulkConverter::new);
        bulkConverters
                .get(RecordType.MBP_10)
                .put(
                        RecordType.TRADES,
                        () -> new WaterfallBulkConverter<>(
                                new Mbp10ToMbp1BulkConverter(), new Mbp1ToTradesBulkConverter()));
        bulkConverters
                .get(RecordType.MBP_10)
                .put(
                        RecordType.BBO_1S,
                        () -> new WaterfallBulkConverter<>(
                                new Mbp10ToMbp1BulkConverter(), new Mbp1ToBbo1sBulkConverter()));
        bulkConverters
                .get(RecordType.MBP_10)
                .put(
                        RecordType.BBO_1M,
                        () -> new WaterfallBulkConverter<>(
                                new Mbp10ToMbp1BulkConverter(), new Mbp1ToBbo1mBulkConverter()));
        bulkConverters
                .get(RecordType.MBP_10)
                .put(
                        RecordType.OHLCV_1S,
                        () -> new WaterfallBulkConverter<>(
                                new Mbp10ToMbp1BulkConverter(),
                                new Mbp1ToTradesBulkConverter(),
                                new TradesToOhlcv1sBulkConverter()));
        bulkConverters
                .get(RecordType.MBP_10)
                .put(
                        RecordType.OHLCV_1M,
                        () -> new WaterfallBulkConverter<>(
                                new Mbp10ToMbp1BulkConverter(),
                                new Mbp1ToTradesBulkConverter(),
                                new TradesToOhlcv1mBulkConverter()));
        bulkConverters
                .get(RecordType.MBP_10)
                .put(
                        RecordType.OHLCV_1H,
                        () -> new WaterfallBulkConverter<>(
                                new Mbp10ToMbp1BulkConverter(),
                                new Mbp1ToTradesBulkConverter(),
                                new TradesToOhlcv1hBulkConverter()));

        // Passthrough converters
        for (var type : RecordType.values()) {
            converters.get(type).put(type, PassthroughConverter::new);
            bulkConverters.get(type).put(type, PassthroughBulkConverter::new);
        }
    }

    public static SchemaConverter<?, ?> getConverter(RecordType source, RecordType target) {
        final var availableConverters = converters.get(source);
        if (!availableConverters.containsKey(target)) {
            throw new IllegalArgumentException("Conversion from " + source + " to " + target + " does not exist");
        }

        return availableConverters.get(target).get();
    }

    public static SchemaBulkConverter<?, ?> getBulkConverter(RecordType source, RecordType target) {
        final var availableConverters = bulkConverters.get(source);
        if (!availableConverters.containsKey(target)) {
            throw new IllegalArgumentException("Conversion from " + source + " to " + target + " does not exist");
        }

        return availableConverters.get(target).get();
    }

    public static boolean hasBulkConverter(RecordType source, RecordType target) {
        return bulkConverters.get(source).containsKey(target);
    }

    public static boolean hasConverter(RecordType source, RecordType target) {
        return converters.get(source).containsKey(target);
    }
}
