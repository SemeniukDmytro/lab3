package com.semeniuk.lab3.binance;

import com.nimbusds.jose.shaded.gson.JsonObject;
import com.nimbusds.jose.shaded.gson.JsonParser;
import com.semeniuk.lab3.protobuf.TradeOuterClass;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;
@Component
public class BinanceService {

    private final AuthenticatedWebSocketHandler webSocketHandler;

    public BinanceService(AuthenticatedWebSocketHandler handler) {
        this.webSocketHandler = handler;
    }

    @PostConstruct
    public void connect() {
        try {
            List<String> symbols = List.of("btcusdt", "ethusdt", "bnbusdt");

            String streamsParam = symbols.stream()
                    .map(s -> s + "@trade")
                    .collect(Collectors.joining("/"));

            String url = "wss://stream.binance.com:9443/stream?streams=" + streamsParam;

            BinanceWebSocketClient client = new BinanceWebSocketClient(url) {
                @Override
                public void onMessage(String message) {
                    try {
                        JsonObject root = JsonParser.parseString(message).getAsJsonObject();
                        String stream = root.get("stream").getAsString();
                        JsonObject data = root.getAsJsonObject("data");

                        TradeOuterClass.Trade.Builder tradeBuilder = TradeOuterClass.Trade.newBuilder()
                                .setStream(stream)
                                .setCoin(data.get("s").getAsString())
                                .setPrice(data.get("p").getAsString())
                                .setQuantity(data.get("q").getAsString())
                                .setTradeTime(data.get("T").getAsLong());

                        TradeOuterClass.Trade tradeProto = tradeBuilder.build();
                        webSocketHandler.broadcastProtobuf(tradeProto.toByteArray());

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };
            client.connect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
