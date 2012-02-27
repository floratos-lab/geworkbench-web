package org.geworkbenchweb.visualizations.client.ui;

import org.thechiselgroup.choosel.protovis.client.PVDomAdapter;

public final class FlareData {

    public static class Unit {

        public Unit[] children;

        public int value;

        public String name;

        public Unit(String name, int value) {
            this.value = value;
            this.name = name;
        }

        public Unit(String name, Unit... children) {
            this.children = children;
            this.name = name;
        }
    }

    public static class UnitDomAdapter implements PVDomAdapter<Unit> {

        public Unit[] getChildren(Unit t) {
            return t.children == null ? new Unit[0] : t.children;
        }

        public String getNodeName(Unit t) {
            return t.name;
        }

        public double getNodeValue(Unit t) {
            return t.value;
        }

    }

    public static Unit data() {
        return new Unit(
                "flare",
                new Unit("analytics", new Unit("cluster", new Unit(
                        "AgglomerativeCluster", 3938), new Unit(
                        "CommunityStructure", 3812), new Unit(
                        "HierarchicalCluster", 6714),
                        new Unit("MergeEdge", 743)), new Unit("graph",
                        new Unit("BetweennessCentrality", 3534), new Unit(
                                "LinkDistance", 5731), new Unit(
                                "MaxFlowMinCut", 7840), new Unit(
                                "ShortestPaths", 5914), new Unit(
                                "SpanningTree", 3416)), new Unit(
                        "optimization", new Unit("AspectRatioBanker", 7074))),
                new Unit("animate", new Unit("Easing", 17010), new Unit(
                        "FunctionSequence", 5842), new Unit("interpolate",
                        new Unit("ArrayInterpolator", 1983), new Unit(
                                "ColorInterpolator", 2047), new Unit(
                                "DateInterpolator", 1375), new Unit(
                                "Interpolator", 8746), new Unit(
                                "MatrixInterpolator", 2202), new Unit(
                                "NumberInterpolator", 1382), new Unit(
                                "ObjectInterpolator", 1629), new Unit(
                                "PointInterpolator", 1675), new Unit(
                                "RectangleInterpolator", 2042)), new Unit(
                        "ISchedulable", 1041), new Unit("Parallel", 5176),
                        new Unit("Pause", 449), new Unit("Scheduler", 5593),
                        new Unit("Sequence", 5534),
                        new Unit("Transition", 9201), new Unit("Transitioner",
                                19975), new Unit("TransitionEvent", 1116),
                        new Unit("Tween", 6006)),
                new Unit("data", new Unit("converters", new Unit("Converters",
                        721), new Unit("DelimitedTextConverter", 4294),
                        new Unit("GraphMLConverter", 9800), new Unit(
                                "IDataConverter", 1314), new Unit(
                                "JSONConverter", 2220)), new Unit("DataField",
                        1759), new Unit("DataSchema", 2165), new Unit(
                        "DataSet", 586), new Unit("DataSource", 3331),
                        new Unit("DataTable", 772), new Unit("DataUtil", 3322)),
                new Unit("display", new Unit("DirtySprite", 8833), new Unit(
                        "LineSprite", 1732), new Unit("RectSprite", 3623),
                        new Unit("TextSprite", 10066)), new Unit("flex",
                        new Unit("FlareVis", 4116)), new Unit("physics",
                        new Unit("DragForce", 1082), new Unit("GravityForce",
                                1336), new Unit("IForce", 319), new Unit(
                                "NBodyForce", 10498),
                        new Unit("Particle", 2822),
                        new Unit("Simulation", 9983), new Unit("Spring", 2213),
                        new Unit("SpringForce", 1681)), new Unit("query",
                        new Unit("AggregateExpression", 1616), new Unit("And",
                                1027), new Unit("Arithmetic", 3891), new Unit(
                                "Average", 891), new Unit("BinaryExpression",
                                2893), new Unit("Comparison", 5103), new Unit(
                                "CompositeExpression", 3677), new Unit("Count",
                                781), new Unit("DateUtil", 4141), new Unit(
                                "Distinct", 933), new Unit("Expression", 5130),
                        new Unit("ExpressionIterator", 3617), new Unit("Fn",
                                3240), new Unit("If", 2732), new Unit("IsA",
                                2039), new Unit("Literal", 1214), new Unit(
                                "Match", 3748), new Unit("Maximum", 843),
                        new Unit("methods", new Unit("add", 593), new Unit(
                                "and", 330), new Unit("average", 287),
                                new Unit("count", 277), new Unit("distinct",
                                        292), new Unit("div", 595), new Unit(
                                        "eq", 594), new Unit("fn", 460),
                                new Unit("gt", 603), new Unit("gte", 625),
                                new Unit("iff", 748), new Unit("isa", 461),
                                new Unit("lt", 597), new Unit("lte", 619),
                                new Unit("max", 283), new Unit("min", 283),
                                new Unit("mod", 591), new Unit("mul", 603),
                                new Unit("neq", 599), new Unit("not", 386),
                                new Unit("or", 323), new Unit("orderby", 307),
                                new Unit("range", 772),
                                new Unit("select", 296),
                                new Unit("stddev", 363), new Unit("sub", 600),
                                new Unit("sum", 280), new Unit("update", 307),
                                new Unit("variance", 335), new Unit("where",
                                        299), new Unit("xor", 354), new Unit(
                                        "_", 264)), new Unit("Minimum", 843),
                        new Unit("Not", 1554), new Unit("Or", 970), new Unit(
                                "Query", 13896), new Unit("Range", 1594),
                        new Unit("StringUtil", 4130), new Unit("Sum", 791),
                        new Unit("Variable", 1124), new Unit("Variance", 1876),
                        new Unit("Xor", 1101)), new Unit("scale", new Unit(
                        "IScaleMap", 2105), new Unit("LinearScale", 1316),
                        new Unit("LogScale", 3151), new Unit("OrdinalScale",
                                3770), new Unit("QuantileScale", 2435),
                        new Unit("QuantitativeScale", 4839), new Unit(
                                "RootScale", 1756), new Unit("Scale", 4268),
                        new Unit("ScaleType", 1821),
                        new Unit("TimeScale", 5833)), new Unit("util",
                        new Unit("Arrays", 8258), new Unit("Colors", 10001),
                        new Unit("Dates", 8217), new Unit("Displays", 12555),
                        new Unit("Filter", 2324), new Unit("Geometry", 10993),
                        new Unit("heap", new Unit("FibonacciHeap", 9354),
                                new Unit("HeapNode", 1233)), new Unit(
                                "IEvaluable", 335),
                        new Unit("IPredicate", 383), new Unit("IValueProxy",
                                874), new Unit("math", new Unit("DenseMatrix",
                                3165), new Unit("IMatrix", 2815), new Unit(
                                "SparseMatrix", 3366)),
                        new Unit("Maths", 17705),
                        new Unit("Orientation", 1486), new Unit("palette",
                                new Unit("ColorPalette", 6367), new Unit(
                                        "Palette", 1229), new Unit(
                                        "ShapePalette", 2059), new Unit(
                                        "SizePalette", 2291)), new Unit(
                                "Property", 5559), new Unit("Shapes", 19118),
                        new Unit("Sort", 6887), new Unit("Stats", 6557),
                        new Unit("Strings", 22026)), new Unit("vis", new Unit(
                        "axis", new Unit("Axes", 1302),
                        new Unit("Axis", 24593), new Unit("AxisGridLine", 652),
                        new Unit("AxisLabel", 636), new Unit("CartesianAxes",
                                6703)), new Unit("controls", new Unit(
                        "AnchorControl", 2138), new Unit("ClickControl", 3824),
                        new Unit("Control", 1353),
                        new Unit("ControlList", 4665), new Unit("DragControl",
                                2649), new Unit("ExpandControl", 2832),
                        new Unit("HoverControl", 4896), new Unit("IControl",
                                763), new Unit("PanZoomControl", 5222),
                        new Unit("SelectionControl", 7862), new Unit(
                                "TooltipControl", 8435)), new Unit("data",
                        new Unit("Data", 20544), new Unit("DataList", 19788),
                        new Unit("DataSprite", 10349), new Unit("EdgeSprite",
                                3301), new Unit("NodeSprite", 19382), new Unit(
                                "render", new Unit("ArrowType", 698), new Unit(
                                        "EdgeRenderer", 5569), new Unit(
                                        "IRenderer", 353), new Unit(
                                        "ShapeRenderer", 2247)), new Unit(
                                "ScaleBinding", 11275), new Unit("Tree", 7147),
                        new Unit("TreeBuilder", 9930)), new Unit("events",
                        new Unit("DataEvent", 2313), new Unit("SelectionEvent",
                                1880), new Unit("TooltipEvent", 1701),
                        new Unit("VisualizationEvent", 1117)), new Unit(
                        "legend", new Unit("Legend", 20859), new Unit(
                                "LegendItem", 4614), new Unit("LegendRange",
                                10530)), new Unit("operator", new Unit(
                        "distortion", new Unit("BifocalDistortion", 4461),
                        new Unit("Distortion", 6314), new Unit(
                                "FisheyeDistortion", 3444)), new Unit(
                        "encoder", new Unit("ColorEncoder", 3179), new Unit(
                                "Encoder", 4060), new Unit("PropertyEncoder",
                                4138), new Unit("ShapeEncoder", 1690),
                        new Unit("SizeEncoder", 1830)), new Unit("filter",
                        new Unit("FisheyeTreeFilter", 5219), new Unit(
                                "GraphDistanceFilter", 3165), new Unit(
                                "VisibilityFilter", 3509)), new Unit(
                        "IOperator", 1286), new Unit("label", new Unit(
                        "Labeler", 9956), new Unit("RadialLabeler", 3899),
                        new Unit("StackedAreaLabeler", 3202)), new Unit(
                        "layout", new Unit("AxisLayout", 6725), new Unit(
                                "BundledEdgeRouter", 3727), new Unit(
                                "CircleLayout", 9317), new Unit(
                                "CirclePackingLayout", 12003), new Unit(
                                "DendrogramLayout", 4853), new Unit(
                                "ForceDirectedLayout", 8411), new Unit(
                                "IcicleTreeLayout", 4864), new Unit(
                                "IndentedTreeLayout", 3174), new Unit("Layout",
                                7881), new Unit("NodeLinkTreeLayout", 12870),
                        new Unit("PieLayout", 2728), new Unit(
                                "RadialTreeLayout", 12348), new Unit(
                                "RandomLayout", 870), new Unit(
                                "StackedAreaLayout", 9121), new Unit(
                                "TreeMapLayout", 9191)), new Unit("Operator",
                        2490), new Unit("OperatorList", 5248), new Unit(
                        "OperatorSequence", 4190), new Unit("OperatorSwitch",
                        2581), new Unit("SortOperator", 2023)), new Unit(
                        "Visualization", 16540)));
    }

}
