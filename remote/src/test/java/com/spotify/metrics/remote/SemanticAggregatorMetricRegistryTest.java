package com.spotify.metrics.remote;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.spotify.metrics.core.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

/**
 * This test tests the SemanticAggregatorMetricRegistry and RemoteSemanticMetricBuilder
 * classes.
 */
@RunWith(MockitoJUnitRunner.class)
public class SemanticAggregatorMetricRegistryTest {

    @Mock
    Remote remote;

    SemanticAggregatorMetricRegistry registry;

    @Before
    public void setUp() throws Exception {
        registry = spy(new SemanticAggregatorMetricRegistry(remote));
    }

    @Test
    public void createMeterTest() {
        assertNotNull(registry.meter(MetricId.build("X").tagged("what", "balls")));
    }

    @Test
    public void meterBump1Test() {
        registry.meter(MetricId.build("X").tagged("what", "balls", "status", "tripping")).mark();
        verify(remote).post("/", "what:balls",
                ImmutableMap.of(
                        "type", "metric",
                        "value", "1",
                        "key", "X",
                        "attributes", ImmutableMap.of(
                                "metric_type", "meter",
                                "status", "tripping",
                                "what", "balls")));
    }

    @Test
    public void meterBump5Test() {
        registry.meter(MetricId.build("X").tagged("what", "balls", "status", "tripping")).mark(5);
        verify(remote).post("/", "what:balls",
                ImmutableMap.of(
                        "type", "metric",
                        "value", "5",
                        "key", "X",
                        "attributes", ImmutableMap.of(
                                "metric_type", "meter",
                                "status", "tripping",
                                "what", "balls")));
    }

    @Test
    public void meterShardTest() {
        registry.meter(
                MetricId.build("X").tagged("what", "balls", "status", "tripping"),
                ImmutableList.of("what", "status")).mark();
        verify(remote).post(anyString(), eq("what:balls,status:tripping"), anyMap());
    }

    @Test
    public void meterUniquenessTest() {
        RemoteMeter a = registry.meter(MetricId.build("X").tagged("what", "balls", "status", "tripping"));
        RemoteMeter b = registry.meter(MetricId.build("X").tagged("what", "balls", "status", "tripping"));
        assert (a == b);
    }

    @Test
    public void createTimerTest() {
        assertNotNull(registry.timer(MetricId.build("X").tagged("what", "balls")));
        assertNotNull(registry.timer(MetricId.build("X").tagged("what", "balls")).time());
    }

    @Test
    public void timerBumpTest() {

        RemoteTimer tm = new SemanticAggregatorTimer(
            MetricId.build("X").tagged("what", "balls", "status", "tripping"),
            ImmutableList.of("what"),
            remote,
            new SemanticAggregatorTimer.TimeSource() {
                long tm;
                @Override
                public long nanoTime() {
                    return tm+= 69;
                }
            });
        tm.time().stop();
        verify(remote).post("/", "what:balls",
                ImmutableMap.of(
                        "type", "metric",
                        "value", "69",
                        "key", "X",
                        "attributes", ImmutableMap.of(
                                "metric_type", "timer",
                                "status", "tripping",
                                "what", "balls")));
    }

    @Test
    public void timerUniquenessTest() {
        RemoteMetric a = registry.timer(MetricId.build("X").tagged("what", "balls", "status", "tripping"));
        RemoteMetric b = registry.timer(MetricId.build("X").tagged("what", "balls", "status", "tripping"));
        assert (a == b);
    }

    @Test
    public void createDerivingMeterTest() {
        assertNotNull(registry.derivingMeter(MetricId.build("X").tagged("what", "balls")));
    }

    @Test
    public void derivingMeterBump1Test() {
        registry.derivingMeter(MetricId.build("X").tagged("what", "balls", "status", "tripping")).mark();
        verify(remote).post("/", "what:balls",
                ImmutableMap.of(
                        "type", "metric",
                        "value", "1",
                        "key", "X",
                        "attributes", ImmutableMap.of(
                                "metric_type", "deriving_meter",
                                "status", "tripping",
                                "what", "balls")));
    }

    @Test
    public void derivingMeterBump5Test() {
        registry.derivingMeter(MetricId.build("X").tagged("what", "balls", "status", "tripping")).mark(5);
        verify(remote).post("/", "what:balls",
                ImmutableMap.of(
                        "type", "metric",
                        "value", "5",
                        "key", "X",
                        "attributes", ImmutableMap.of(
                                "metric_type", "deriving_meter",
                                "status", "tripping",
                                "what", "balls")));
    }

    @Test
    public void derivingMeterShardTest() {
        registry.derivingMeter(
                MetricId.build("X").tagged("what", "balls", "status", "tripping"),
                ImmutableList.of("what", "status")).mark();
        verify(remote).post(anyString(), eq("what:balls,status:tripping"), anyMap());
    }

    @Test
    public void derivingMeterUniquenessTest() {
        RemoteDerivingMeter a = registry.derivingMeter(MetricId.build("X").tagged("what", "balls", "status", "tripping"));
        RemoteDerivingMeter b = registry.derivingMeter(MetricId.build("X").tagged("what", "balls", "status", "tripping"));
        assert (a == b);
    }

    @Test
    public void createHistogramTest() {
        assertNotNull(registry.histogram(MetricId.build("X").tagged("what", "balls")));
    }

    @Test
    public void histogramBumpTest() {
        registry.histogram(MetricId.build("X").tagged("what", "balls", "status", "tripping")).update(5);
        verify(remote).post("/", "what:balls",
                ImmutableMap.of(
                        "type", "metric",
                        "value", "5",
                        "key", "X",
                        "attributes", ImmutableMap.of(
                                "metric_type", "histogram",
                                "status", "tripping",
                                "what", "balls")));
    }

    @Test
    public void histogramShardTest() {
        registry.histogram(
                MetricId.build("X").tagged("what", "balls", "status", "tripping"),
                ImmutableList.of("what", "status")).update(3);
        verify(remote).post(anyString(), eq("what:balls,status:tripping"), anyMap());
    }

    @Test
    public void histogramUniquenessTest() {
        RemoteHistogram a = registry.histogram(MetricId.build("X").tagged("what", "balls", "status", "tripping"));
        RemoteHistogram b = registry.histogram(MetricId.build("X").tagged("what", "balls", "status", "tripping"));
        assert (a == b);
    }

}
