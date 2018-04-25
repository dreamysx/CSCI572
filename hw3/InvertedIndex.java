import java.io.IOException;
import java.util.*;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapred.OutputCollector;

public class InvertedIndex {

    public static class TokenizerMapper
            extends Mapper<LongWritable, Text, Text, Text>{

        private Text id = new Text();
        private Text word = new Text();

        public void map(LongWritable key, Text value, Context context
        ) throws IOException, InterruptedException {
            String docid = value.toString().split("\t")[0];
            if (docid == "6818880") {
                id.set(docid);
                String docvalue = value.toString().split("\t")[1];
                StringTokenizer itr = new StringTokenizer(docvalue);
                while (itr.hasMoreTokens()) {
                    word.set(itr.nextToken());
                    context.write(word, id);
                }
            }
        }
    }

    public static class IntSumReducer
            extends Reducer<Text,Text,Text,Text> {
        private Text result = new Text();

        public void reduce(Text key, Iterable<Text> values, Context context
        ) throws IOException, InterruptedException {
            Map<String, Integer> record = new HashMap<>();
            for (Text val : values) {
                String cur = val.toString();
                record.put(cur, record.getOrDefault(cur, 0) + 1);
            }
            StringBuilder sb = new StringBuilder();
            Boolean first = true;
            for (Map.Entry me: record.entrySet()) {
                if (!first) {
                    sb.append("\t");
                }
                sb.append(me.getKey());
                sb.append(":");
                sb.append(me.getValue());
                first = false;
            }

            result.set(sb.toString());
            context.write(key, result);
        }
    }

    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "inverted index");
        job.setJarByClass(InvertedIndex.class);
        job.setMapperClass(TokenizerMapper.class);
        job.setReducerClass(IntSumReducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);
        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));
        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}