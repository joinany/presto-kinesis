/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.facebook.presto.kinesis;

import java.util.List;
import javax.inject.Named;
import com.amazonaws.services.kinesis.model.Shard;
import com.facebook.presto.spi.ConnectorSplitManager;
import com.facebook.presto.spi.ConnectorSplitSource;
import com.facebook.presto.spi.ConnectorSession;
import com.facebook.presto.spi.ConnectorTableLayoutHandle;
import com.facebook.presto.spi.ConnectorSplit;
import com.facebook.presto.spi.FixedSplitSource;
import com.google.common.collect.ImmutableList;
import static com.facebook.presto.kinesis.KinesisHandleResolver.convertLayout;
import com.google.inject.Inject;

/**
 *
 * Split data chunk from kinesis Stream to multiple small chunks for parallelization and distribution to multiple Presto workers.
 * By default, each shard of Kinesis Stream forms one Kinesis Split
 *
 */
public class KinesisSplitManager
        implements ConnectorSplitManager
{
    private final String connectorId;
    private final KinesisHandleResolver handleResolver;
    private final KinesisClientManager clientManager;

    @Inject
    public  KinesisSplitManager(@Named("connectorId") String connectorId,
            KinesisHandleResolver handleResolver,
            KinesisClientManager clientManager)
    {
        this.connectorId = connectorId;
        this.handleResolver = handleResolver;
        this.clientManager = clientManager;
    }

    @Override
    public ConnectorSplitSource getSplits(ConnectorSession session, ConnectorTableLayoutHandle layout)
    {
        KinesisTableHandle kinesisTableHandle = convertLayout(layout).getTable();

        ImmutableList.Builder<ConnectorSplit> builder = ImmutableList.builder();

        String streamName = kinesisTableHandle.getStreamName();
        List<Shard> shards = clientManager.getClient().describeStream(streamName).getStreamDescription().getShards();
        for (Shard shard : shards) {
            KinesisShard kinesisShard = new KinesisShard(streamName, shard);
            KinesisSplit split = new KinesisSplit(connectorId,
                    kinesisShard.getStreamName(),
                    kinesisTableHandle.getMessageDataFormat(),
                    kinesisShard.getPartitionId(),
                    kinesisShard.getShard().getSequenceNumberRange().getStartingSequenceNumber(),
                    kinesisShard.getShard().getSequenceNumberRange().getEndingSequenceNumber());
            builder.add(split);
        }

        return new FixedSplitSource(connectorId, builder.build());
    }
}
