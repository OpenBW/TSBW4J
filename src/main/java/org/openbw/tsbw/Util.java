package org.openbw.tsbw;

import java.util.Collections;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

import org.openbw.bwapi4j.unit.MobileUnit;
import org.openbw.bwapi4j.unit.Unit;

public class Util {

	public static <T extends Unit> Collector<T, Group<T>, Group<T>> toGroup() {
		return new Collector<T, Group<T>, Group<T>>(){

			@Override
			public Supplier<Group<T>> supplier() {
				return (Supplier<Group<T>>) Group::new;
			}

			@Override
			public BiConsumer<Group<T>, T> accumulator() {
				return (group, value) -> group.add(value);
			}
			
			@Override
			public BinaryOperator<Group<T>> combiner() {
				return (left, right) -> { left.addAll(right); return left; };
			}

			@Override
			public Function<Group<T>, Group<T>> finisher() {
				return Function.identity();
			}

			@Override
			public Set<Characteristics> characteristics() {
				return Collections.singleton(Characteristics.IDENTITY_FINISH);
			}
		};
    }
	
	public static <T extends MobileUnit> Collector<T, Squad<T>, Squad<T>> toSquad() {
		return new Collector<T, Squad<T>, Squad<T>>(){

			@Override
			public Supplier<Squad<T>> supplier() {
				return (Supplier<Squad<T>>) Squad::new;
			}

			@Override
			public BiConsumer<Squad<T>, T> accumulator() {
				return (group, value) -> group.add(value);
			}
			
			@Override
			public BinaryOperator<Squad<T>> combiner() {
				return (left, right) -> { left.addAll(right); return left; };
			}

			@Override
			public Function<Squad<T>, Squad<T>> finisher() {
				return Function.identity();
			}

			@Override
			public Set<Characteristics> characteristics() {
				return Collections.singleton(Characteristics.IDENTITY_FINISH);
			}
		};
    }
}
