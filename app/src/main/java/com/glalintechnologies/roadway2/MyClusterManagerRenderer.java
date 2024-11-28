package com.glalintechnologies.roadway2;

import android.content.Context;
import android.graphics.Bitmap;

import com.google.android.gms.maps.GoogleMap;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;

public class MyClusterManagerRenderer extends DefaultClusterRenderer<ClusterMarker> {

    private final IconGenerator iconGenerator;
    private final int markerWidth;
    private final int markerHeight;

    public MyClusterManagerRenderer(Context context, GoogleMap map, ClusterManager<ClusterMarker> clusterManager,
                                    IconGenerator iconGenerator, int markerWidth, int markerHeight) {
        super(context, map, clusterManager);
        this.iconGenerator = iconGenerator;
        this.markerWidth = markerWidth;
        this.markerHeight = markerHeight;
    }

    @Override
    protected void onBeforeClusterItemRendered(ClusterMarker item, com.google.android.gms.maps.model.MarkerOptions markerOptions) {
        // Generate custom icons using IconGenerator
        iconGenerator.setContentView(item.getMarkerView()); // Assuming the marker has a custom view
        iconGenerator.setStyle(IconGenerator.STYLE_DEFAULT);

        Bitmap iconBitmap = iconGenerator.makeIcon();
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(iconBitmap, markerWidth, markerHeight, false);

        markerOptions.icon(com.google.android.gms.maps.model.BitmapDescriptorFactory.fromBitmap(resizedBitmap))
                .title(item.getTitle())
                .snippet(item.getSnippet());
    }

    @Override
    protected void onBeforeClusterRendered(Cluster<ClusterMarker> cluster, com.google.android.gms.maps.model.MarkerOptions markerOptions) {
        // Customize cluster markers if needed
        super.onBeforeClusterRendered(cluster, markerOptions);
    }

    @Override
    protected boolean shouldRenderAsCluster(Cluster<ClusterMarker> cluster) {
        // Define the threshold for clustering
        return cluster.getSize() > 3; // Only cluster if there are more than 3 markers
    }
}
