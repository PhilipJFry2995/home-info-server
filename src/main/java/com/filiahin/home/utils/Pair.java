package com.filiahin.home.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class Pair {
        private String key;
        private String value;

        public static Pair of(final String key, final String value) {
            return new Pair(key, value);
        }
}
