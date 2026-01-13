package group.gnometrading.schemas.converters;

import group.gnometrading.schemas.SchemaType;
import group.gnometrading.schemas.converters.mbp1.*;
import group.gnometrading.schemas.converters.mbp10.MBP10ToMBP1BulkConverter;
import group.gnometrading.schemas.converters.mbp10.MBP10ToMBP1Converter;
import group.gnometrading.schemas.converters.trades.*;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class SchemaConversionRegistry {

    private static final Map<SchemaType, Map<SchemaType, Supplier<SchemaConverter<?, ?>>>> converters = new HashMap<>();
    private static final Map<SchemaType, Map<SchemaType, Supplier<SchemaBulkConverter<?, ?>>>> bulkConverters = new HashMap<>();

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
        converters.get(SchemaType.MBP_10).put(SchemaType.MBP_1, MBP10ToMBP1Converter::new);
        converters.get(SchemaType.MBP_10).put(SchemaType.TRADES, () -> new WaterfallConverter<>(
                new MBP10ToMBP1Converter(),
                new MBP1ToTradesConverter()
        ));
        converters.get(SchemaType.MBP_10).put(SchemaType.BBO_1S, () -> new WaterfallConverter<>(
                new MBP10ToMBP1Converter(),
                new MBP1ToBBO1SConverter()
        ));
        converters.get(SchemaType.MBP_10).put(SchemaType.BBO_1M, () -> new WaterfallConverter<>(
                new MBP10ToMBP1Converter(),
                new MBP1ToBBO1MConverter()
        ));
        converters.get(SchemaType.MBP_10).put(SchemaType.OHLCV_1S, () -> new WaterfallConverter<>(
                new MBP10ToMBP1Converter(),
                new MBP1ToTradesConverter(),
                new TradesToOHLCV1SConverter()
        ));
        converters.get(SchemaType.MBP_10).put(SchemaType.OHLCV_1M, () -> new WaterfallConverter<>(
                new MBP10ToMBP1Converter(),
                new MBP1ToTradesConverter(),
                new TradesToOHLCV1MConverter()
        ));
        converters.get(SchemaType.MBP_10).put(SchemaType.OHLCV_1H, () -> new WaterfallConverter<>(
                new MBP10ToMBP1Converter(),
                new MBP1ToTradesConverter(),
                new TradesToOHLCV1HConverter()
        ));
        bulkConverters.get(SchemaType.MBP_10).put(SchemaType.MBP_1, MBP10ToMBP1BulkConverter::new);
        bulkConverters.get(SchemaType.MBP_10).put(SchemaType.TRADES, () -> new WaterfallBulkConverter<>(
                new MBP10ToMBP1BulkConverter(),
                new MBP1ToTradesBulkConverter()
        ));
        bulkConverters.get(SchemaType.MBP_10).put(SchemaType.BBO_1S, () -> new WaterfallBulkConverter<>(
                new MBP10ToMBP1BulkConverter(),
                new MBP1ToBBO1SBulkConverter()
        ));
        bulkConverters.get(SchemaType.MBP_10).put(SchemaType.BBO_1M, () -> new WaterfallBulkConverter<>(
                new MBP10ToMBP1BulkConverter(),
                new MBP1ToBBO1MBulkConverter()
        ));
        bulkConverters.get(SchemaType.MBP_10).put(SchemaType.OHLCV_1S, () -> new WaterfallBulkConverter<>(
                new MBP10ToMBP1BulkConverter(),
                new MBP1ToTradesBulkConverter(),
                new TradesToOHLCV1SBulkConverter()
        ));
        bulkConverters.get(SchemaType.MBP_10).put(SchemaType.OHLCV_1M, () -> new WaterfallBulkConverter<>(
                new MBP10ToMBP1BulkConverter(),
                new MBP1ToTradesBulkConverter(),
                new TradesToOHLCV1MBulkConverter()
        ));
        bulkConverters.get(SchemaType.MBP_10).put(SchemaType.OHLCV_1H, () -> new WaterfallBulkConverter<>(
                new MBP10ToMBP1BulkConverter(),
                new MBP1ToTradesBulkConverter(),
                new TradesToOHLCV1HBulkConverter()
        ));

        // Passthrough converters
        for (var type : SchemaType.values()) {
            converters.get(type).put(type, PassthroughConverter::new);
            bulkConverters.get(type).put(type, PassthroughBulkConverter::new);
        }
    }

    public static SchemaConverter<?, ?> getConverter(SchemaType source, SchemaType target) {
        final var availableConverters = converters.get(source);
        if (!availableConverters.containsKey(target)) {
            throw new IllegalArgumentException(String.format("Conversion from %s to %s does not exist", source, target));
        }

        return availableConverters.get(target).get();
    }

    public static SchemaBulkConverter<?, ?> getBulkConverter(SchemaType source, SchemaType target) {
        final var availableConverters = bulkConverters.get(source);
        if (!availableConverters.containsKey(target)) {
            throw new IllegalArgumentException(String.format("Conversion from %s to %s does not exist", source, target));
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
