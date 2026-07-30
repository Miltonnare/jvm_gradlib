package com.aura.pipeline;

import com.aura.internal.Preconditions;
import java.util.ArrayList;
import java.util.List;

/**
 * Sequential execution runtime for conversion pipeline stages.
 */
public class StageExecutor {
    private final List<TransformationPipeline.Stage<?, ?>> stages = new ArrayList<>();
    private final StageContext context = new StageContext();

    public void addStage(TransformationPipeline.Stage<?, ?> stage) {
        Preconditions.checkNotNull(stage, "Stage cannot be null");
        stages.add(stage);
    }

    /**
     * Executes the registered stages sequentially.
     */
    @SuppressWarnings("unchecked")
    public Object execute(Object input) {
        Preconditions.checkNotNull(input, "Pipeline input cannot be null");
        Object current = input;

        for (int i = 0; i < stages.size(); i++) {
            TransformationPipeline.Stage<Object, Object> stage = (TransformationPipeline.Stage<Object, Object>) stages.get(i);
            long start = System.nanoTime();
            
            try {
                current = stage.process(current);
            } catch (Exception e) {
                throw new TransformationPipeline.PipelineException("Pipeline executor failed at stage: " + stage.getClass().getSimpleName(), e);
            }
            
            long durationMs = (System.nanoTime() - start) / 1_000_000;
            context.recordTiming("Stage_" + i + "_" + stage.getClass().getSimpleName(), durationMs);
        }

        return current;
    }

    public StageContext getContext() {
        return context;
    }

    public void clear() {
        stages.clear();
        context.clear();
    }
}
