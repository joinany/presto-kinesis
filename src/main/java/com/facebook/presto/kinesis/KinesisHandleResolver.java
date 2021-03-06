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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.facebook.presto.spi.ConnectorHandleResolver;
import com.facebook.presto.spi.ConnectorTableHandle;
import com.facebook.presto.spi.ConnectorTableLayoutHandle;
import com.facebook.presto.spi.ColumnHandle;
import com.facebook.presto.spi.ConnectorIndexHandle;
import com.facebook.presto.spi.ConnectorSplit;
import com.facebook.presto.spi.ConnectorOutputTableHandle;
import com.facebook.presto.spi.ConnectorInsertTableHandle;

public class KinesisHandleResolver
        implements ConnectorHandleResolver
{
    @Override
    public Class<? extends ConnectorTableHandle> getTableHandleClass()
    {
        return KinesisTableHandle.class;
    }

    @Override
    public Class<? extends ConnectorTableLayoutHandle> getTableLayoutHandleClass()
    {
        return KinesisTableLayoutHandle.class;
    }

    @Override
    public Class<? extends ColumnHandle> getColumnHandleClass()
    {
        return KinesisColumnHandle.class;
    }

    @Override
    public Class<? extends ConnectorIndexHandle> getIndexHandleClass()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Class<? extends ConnectorSplit> getSplitClass()
    {
        return KinesisSplit.class;
    }

    @Override
    public Class<? extends ConnectorOutputTableHandle> getOutputTableHandleClass()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Class<? extends ConnectorInsertTableHandle> getInsertTableHandleClass()
    {
        throw new UnsupportedOperationException();
    }

    static KinesisTableHandle convertTableHandle(ConnectorTableHandle tableHandle)
    {
        checkNotNull(tableHandle, "tableHandle is null");
        checkArgument(tableHandle instanceof KinesisTableHandle, "tableHandle is not an instance of KinesisTableHandle");
        KinesisTableHandle kinesisTableHandle = (KinesisTableHandle) tableHandle;
        return kinesisTableHandle;
    }

    static KinesisColumnHandle convertColumnHandle(ColumnHandle columnHandle)
    {
        checkNotNull(columnHandle, "columnHandle is null");
        checkArgument(columnHandle instanceof KinesisColumnHandle, "columnHandle is not an instance of KinesisColumnHandle");
        KinesisColumnHandle kinesisColumnHandle = (KinesisColumnHandle) columnHandle;
        return kinesisColumnHandle;
    }

    static KinesisSplit convertSplit(ConnectorSplit split)
    {
        checkNotNull(split, "split is null");
        checkArgument(split instanceof KinesisSplit, "split is not an instance of KinesisSplit");
        KinesisSplit kinesisSplit = (KinesisSplit) split;
        return kinesisSplit;
    }

    static KinesisTableLayoutHandle convertLayout(ConnectorTableLayoutHandle layout)
    {
        checkNotNull(layout, "layout is null");
        checkArgument(layout instanceof KinesisTableLayoutHandle, "layout is not an instance of KinesisTableLayoutHandle");
        return (KinesisTableLayoutHandle) layout;
    }
}
