package group.gnometrading.schemas.converters;

import group.gnometrading.schemas.SchemaType;
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

    private static final Map<SchemaType, Map<SchemaType, Supplier<SchemaConverter<?, ?>>>> converters = new HashMap<>();
    private static final Map<SchemaType, Map<SchemaType, Supplier<SchemaBulkConverter<?, ?>>>> bulkConverters =
            new HashMap<>();

    static {
        converters.put(SchemaType.MBO, new HashMap<>());
        converters.put(SchemaType.MBP_10, new HashMap<>());
        converters.put(SchemaType.MBP_1, new HashMap<>());
        converters.put(SchemaType.BBO_1S, new HashMap<>());
        converters.put(SchemaType.BBO_1M, new HashMap<>());
        converters.put(SchemaType.TRADES, new HashMap<>());
        converters.put(SchemaType.OHLCV_1S, new HashMap<>());
        converters.put(SchemaType.OHLCV_1M, new HashMap<>());
        converters.put(SchemaType.OHLCV_1H, new HashMap<>());
        bulkConverters.put(SchemaType.MBO, new HashMap<>());
        bulkConverters.put(SchemaType.MBP_10, new HashMap<>());
        bulkConverters.put(SchemaType.MBP_1, new HashMap<>());
        bulkConverters.put(SchemaType.BBO_1S, new HashMap<>());
        bulkConverters.put(SchemaType.BBO_1M, new HashMap<>());
        bulkConverters.put(SchemaType.TRADES, new HashMap<>());
        bulkConverters.put(SchemaType.OHLCV_1S, new HashMap<>());
        bulkConverters.put(SchemaType.OHLCV_1M, new HashMap<>());
        bulkConverters.put(SchemaType.OHLCV_1H, new HashMap<>());

        // MBP10 converters
        converters.get(SchemaType.MBP_10).put(SchemaType.MBP_1, Mbp10ToMbp1Converter::new);
        converters
                .get(SchemaType.MBP_10)
                .put(
                        SchemaType.TRADES,
                        () -> new WaterfallConverter<>(new Mbp10ToMbp1Converter(), new Mbp1ToTradesConverter()));
        converters
                .get(SchemaType.MBP_10)
                .put(
                        SchemaType.BBO_1S,
                        () -> new WaterfallConverter<>(new Mbp10ToMbp1Converter(), new Mbp1ToBbo1sConverter()));
        converters
                .get(SchemaType.MBP_10)
                .put(
                        SchemaType.BBO_1M,
                        () -> new WaterfallConverter<>(new Mbp10ToMbp1Converter(), new Mbp1ToBbo1mConverter()));
        converters
                .get(SchemaType.MBP_10)
                .put(
                        SchemaType.OHLCV_1S,
                        () -> new WaterfallConverter<>(
                                new Mbp10ToMbp1Converter(),
                                new Mbp1ToTradesConverter(),
                                new TradesToOhlcv1sConverter()));
        converters
                .get(SchemaType.MBP_10)
                .put(
                        SchemaType.OHLCV_1M,
                        () -> new WaterfallConverter<>(
                                new Mbp10ToMbp1Converter(),
                                new Mbp1ToTradesConverter(),
                                new TradesToOhlcv1mConverter()));
        converters
                .get(SchemaType.MBP_10)
                .put(
                        SchemaType.OHLCV_1H,
                        () -> new WaterfallConverter<>(
                                new Mbp10ToMbp1Converter(),
                                new Mbp1ToTradesConverter(),
                                new TradesToOhlcv1hConverter()));
        bulkConverters.get(SchemaType.MBP_10).put(SchemaType.MBP_1, Mbp10ToMbp1BulkConverter::new);
        bulkConverters
                .get(SchemaType.MBP_10)
                .put(
                        SchemaType.TRADES,
                        () -> new WaterfallBulkConverter<>(
                                new Mbp10ToMbp1BulkConverter(), new Mbp1ToTradesBulkConverter()));
        bulkConverters
                .get(SchemaType.MBP_10)
                .put(
                        SchemaType.BBO_1S,
                        () -> new WaterfallBulkConverter<>(
                                new Mbp10ToMbp1BulkConverter(), new Mbp1ToBbo1sBulkConverter()));
        bulkConverters
                .get(SchemaType.MBP_10)
                .put(
                        SchemaType.BBO_1M,
                        () -> new WaterfallBulkConverter<>(
                                new Mbp10ToMbp1BulkConverter(), new Mbp1ToBbo1mBulkConverter()));
        bulkConverters
                .get(SchemaType.MBP_10)
                .put(
                        SchemaType.OHLCV_1S,
                        () -> new WaterfallBulkConverter<>(
                                new Mbp10ToMbp1BulkConverter(),
                                new Mbp1ToTradesBulkConverter(),
                                new TradesToOhlcv1sBulkConverter()));
        bulkConverters
                .get(SchemaType.MBP_10)
                .put(
                        SchemaType.OHLCV_1M,
                        () -> new WaterfallBulkConverter<>(
                                new Mbp10ToMbp1BulkConverter(),
                                new Mbp1ToTradesBulkConverter(),
                                new TradesToOhlcv1mBulkConverter()));
        bulkConverters
                .get(SchemaType.MBP_10)
                .put(
                        SchemaType.OHLCV_1H,
                        () -> new WaterfallBulkConverter<>(
                                new Mbp10ToMbp1BulkConverter(),
                                new Mbp1ToTradesBulkConverter(),
                                new TradesToOhlcv1hBulkConverter()));

        // Passthrough converters
        for (var type : SchemaType.values()) {
            converters.get(type).put(type, PassthroughConverter::new);
            bulkConverters.get(type).put(type, PassthroughBulkConverter::new);
        }
    }

    public static SchemaConverter<?, ?> getConverter(SchemaType source, SchemaType target) {
        final var availableConverters = converters.get(source);
        if (!availableConverters.containsKey(target)) {
            throw new IllegalArgumentException("Conversion from " + source + " to " + target + " does not exist");
        }

        return availableConverters.get(target).get();
    }

    public static SchemaBulkConverter<?, ?> getBulkConverter(SchemaType source, SchemaType target) {
        final var availableConverters = bulkConverters.get(source);
        if (!availableConverters.containsKey(target)) {
            throw new IllegalArgumentException("Conversion from " + source + " to " + target + " does not exist");
        }

        return availableConverters.get(target).get();
    }

    public static boolean hasBulkConverter(SchemaType source, SchemaType target) {
        return bulkConverters.get(source).containsKey(target);
    }

    public static boolean hasConverter(SchemaType source, SchemaType target) {
        return converters.get(source).containsKey(target);
    }
}
