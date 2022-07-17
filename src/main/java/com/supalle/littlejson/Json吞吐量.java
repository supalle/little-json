package com.supalle.littlejson;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.lang.reflect.Type;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@BenchmarkMode({Mode.Throughput})
@OutputTimeUnit(TimeUnit.SECONDS)
@State(Scope.Thread)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@Threads(4)
public class Json吞吐量 {

    @State(Scope.Benchmark)
    public static class BenchmarkState {
        String json = "[{\"name\":\"张三\",\"age\":\"18\",\"birthday\":\"2022-11-23\",\"address\":\"北京\"},{\"name\":\"李四\",\"age\":\"20\",\"birthday\":\"2022-11-23\",\"address\":\"福建\"}]";

        //        String json = "[{\"name\":\"张三\",\"age\":18,\"birthday\":\"2022-11-23\",\"address\":\"北京\"},{\"name\":\"李四\",\"age\":20,\"birthday\":\"2022-11-23\",\"address\":\"福建\",\"children\":[{\"name\":\"张三\",\"age\":18,\"birthday\":\"2022-11-23\",\"address\":\"北京\"},{\"name\":\"李四\",\"age\":20,\"birthday\":\"2022-11-23\",\"address\":\"福建\",\"children\":[{\"name\":\"张三\",\"age\":18,\"birthday\":\"2022-11-23\",\"address\":\"北京\"},{\"name\":\"李四\",\"age\":20,\"birthday\":\"2022-11-23\",\"address\":\"福建\",\"children\":[{\"name\":\"张三\",\"age\":18,\"birthday\":\"2022-11-23\",\"address\":\"北京\"},{\"name\":\"李四\",\"age\":20,\"birthday\":\"2022-11-23\",\"address\":\"福建\",\"children\":[{\"name\":\"张三\",\"age\":18,\"birthday\":\"2022-11-23\",\"address\":\"北京\"},{\"name\":\"李四\",\"age\":20,\"birthday\":\"2022-11-23\",\"address\":\"福建\",\"children\":[{\"name\":\"张三\",\"age\":18,\"birthday\":\"2022-11-23\",\"address\":\"北京\"},{\"name\":\"李四\",\"age\":20,\"birthday\":\"2022-11-23\",\"address\":\"福建\",\"children\":[{\"name\":\"张三\",\"age\":18,\"birthday\":\"2022-11-23\",\"address\":\"北京\"},{\"name\":\"李四\",\"age\":20,\"birthday\":\"2022-11-23\",\"address\":\"福建\",\"children\":[{\"name\":\"张三\",\"age\":18,\"birthday\":\"2022-11-23\",\"address\":\"北京\"},{\"name\":\"李四\",\"age\":20,\"birthday\":\"2022-11-23\",\"address\":\"福建\",\"children\":[{\"name\":\"张三\",\"age\":18,\"birthday\":\"2022-11-23\",\"address\":\"北京\"},{\"name\":\"李四\",\"age\":20,\"birthday\":\"2022-11-23\",\"address\":\"福建\"}]}]}]}]}]}]}]}]}]";
        Gson GSON = new Gson();
        ObjectMapper objectMapper = new ObjectMapper();

        Type TYPE = new TypeToken<List<Student>>() {
        }.getType();

        TypeReference<List<Student>> TYPE_REFERENCE = new TypeReference<List<Student>>() {
        };
        int number = (int) Math.random();

    }


    @Benchmark
    public Object jackson(BenchmarkState state) throws JsonProcessingException {
        return state.objectMapper.readValue(state.json, List.class);
    }

    @Benchmark
    public Object fastjson(BenchmarkState state) {
        return JSON.parseArray(state.json);
    }

//    @Benchmark
//    public List<Student> fastjson2(BenchmarkState state) {
//        return com.alibaba.fastjson2.JSON.parseObject(state.json, state.TYPE);
//    }

    @Benchmark
    public Object gson(BenchmarkState state) {
        return state.GSON.fromJson(state.json, List.class);
    }

    @Benchmark
    public Object jsonTools(BenchmarkState state) {
        return new SupalleJsonParser2(state.json).parseObject();
    }
    @Benchmark
    public Object LittleJsonParser(BenchmarkState state) {
        return new LittleJsonParser(state.json).parse();
    }

    @Benchmark
    public Object fastjson2ToMap(BenchmarkState state) {
        return com.alibaba.fastjson2.JSON.parseArray(state.json);
    }

//    @Benchmark
//    public Object switch1(BenchmarkState state) {
//        return TestSwitch.parse1(state.number);
//    }
//
//    @Benchmark
//    public Object switch2(BenchmarkState state) {
//        return TestSwitch.parse2(state.number);
//    }
//
//    @Benchmark
//    public int rightMove(BenchmarkState state) {
//        return state.number >> 2;
//    }
//
//    @Benchmark
//    public int and(BenchmarkState state) {
//        return state.number & (3<<30);
//    }
//
//    @Benchmark
//    public int leftMove1(BenchmarkState state) {
//        return (state.number << 2) | 2;
//    }
//
//    @Benchmark
//    public int leftMove2(BenchmarkState state) {
//        return (state.number << 2) + 2;
//    }
//
//    @Benchmark
//    public int leftMove3(BenchmarkState state) {
//        return (2 << 30) | state.number;
//    }
//
//    @Benchmark
//    public int leftMove4(BenchmarkState state) {
//        return (2 << 30) + state.number;
//    }

    public static void main(String[] args) throws Exception {
        Options opts = new OptionsBuilder()
                .include(Json吞吐量.class.getSimpleName())
                // .resultFormat(ResultFormatType.CSV)
                // 预热3轮
                .warmupIterations(6)
                // 度量10轮
                .measurementIterations(5)
                .mode(Mode.Throughput)
                .forks(1)
                .threads(1)
                .build();

        new Runner(opts).run();
    }

    private static class Student {
        String name;
        int age;
        Date birthday;
        String address;

        public Student() {
        }


        public Student(String name, int age, Date birthday, String address) {
            this.name = name;
            this.age = age;
            this.birthday = birthday;
            this.address = address;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }

        public Date getBirthday() {
            return birthday;
        }

        public void setBirthday(Date birthday) {
            this.birthday = birthday;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }
    }

}
