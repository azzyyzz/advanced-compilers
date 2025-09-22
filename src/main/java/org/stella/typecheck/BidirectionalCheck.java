package org.stella.typecheck;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

// import org.stella.typecheck.VisitTypeCheck.RecordFieldTypeVisitor;
import org.syntax.stella.Absyn.*;

public class BidirectionalCheck {
    boolean ambiguousType = false;
    boolean subtyping = false;
    boolean recon = false;
    Unification unif = new Unification();

    public Type check(Type expectedType, Type actualType) {

        if (subtyping) {
            checkSubtype(expectedType, actualType);
        }

        if (!ambiguousType) {
            System.out.println("ambiguousType");
            if (expectedType instanceof TypeBottom) {
                return actualType;
            } else if (actualType instanceof TypeBottom) {
                return expectedType;
            }
        }

        if (expectedType instanceof TypeFun expectedFun && actualType instanceof TypeFun actualFun) {
            if (expectedFun.listtype_.size() != actualFun.listtype_.size()) {
                throw new IllegalArgumentException("[ERROR_UNEXPECTED_TYPE_FOR_EXPRESSION]");
            }
            System.out.println("IDDDIDIDIIDIDIDIDIIDIIDIID1\n");
            Type return_ = check(expectedFun.type_, actualFun.type_);
            System.out.println("IDDDIDIDIIDIDIDIDIIDIIDIID2\n " + actualFun.listtype_.size());
            ListType list = new ListType();
            for (int i = 0; i < expectedFun.listtype_.size(); i++) {
                Type a = expectedFun.listtype_.get(i);
                Type b = actualFun.listtype_.get(i);
                System.out.println(a.getClass() + " " + b.getClass());
                list.add(check(b, a));
            }
            System.out.println("IDDDIDIDIIDIDIDIDIIDIIDIID3\n");
            return new TypeFun(list, return_);
        } else if (expectedType instanceof TypeSum expectedSum && actualType instanceof TypeSum actualSum) {
            return new TypeSum(check(expectedSum.type_1, actualSum.type_1),
                    check(expectedSum.type_2, actualSum.type_2));
        } else if (expectedType instanceof TypeList expectedList && actualType instanceof TypeList actualList) {
            System.out.println("CheckList");
            return new TypeList(check(expectedList.type_, actualList.type_));
        } else if (expectedType instanceof TypeRef expectedRef && actualType instanceof TypeRef actualRef) {
            return new TypeRef(check(expectedRef.type_, actualRef.type_));
        } else if (expectedType instanceof TypeVariant expectedVariant
                && actualType instanceof TypeVariant actualVariant) {
            Map<String, Type> expectedMap = expectedVariant.listvariantfieldtype_.stream()
                    .map(AVariantFieldType.class::cast).collect(
                            Collectors.toMap(a -> a.stellaident_, a -> {
                                if (a.optionaltyping_ instanceof NoTyping)
                                    return new TypeBottom();
                                return ((SomeTyping) a.optionaltyping_).type_;
                            }));
            Map<String, Type> actualMap = actualVariant.listvariantfieldtype_.stream()
                    .map(AVariantFieldType.class::cast).collect(
                            Collectors.toMap(a -> a.stellaident_, a -> {
                                if (a.optionaltyping_ instanceof NoTyping)
                                    return new TypeBottom();
                                return ((SomeTyping) a.optionaltyping_).type_;
                            }));

            for (var i : actualMap.entrySet()) {
                String ident = i.getKey();
                Type type = i.getValue();

                if (!subtyping && !expectedMap.containsKey(ident)) {
                    throw new IllegalArgumentException("[ERROR_UNEXPECTED_VARIANT_LABEL]");
                }

                Type type2 = expectedMap.get(ident);
                check(type2, type);
            }
        }
        // else if (expectedType instanceof TypeRecord expectedRecord && actualType
        // instanceof TypeRecord actualRecord) {

        // }
        // else if (expectedType instanceof TypeTuple expectedTuple && actualType
        // instanceof TypeTuple actualTuple) {
        // if (expectedTuple.listtype_.size() != actualTuple.listtype_.size()) {
        // throw new IllegalArgumentException("[ERROR_UNEXPECTED_TYPE_FOR_EXPRESSION]");
        // }

        // ListType list = new ListType();
        // for (int i = 0; i < expectedTuple.listtype_.size(); i ++) {
        // Type a = expectedTuple.listtype_.get(i);
        // Type b = expectedTuple.listtype_.get(i);
        // list.add(check(a, b));
        // }
        // return new TypeTuple(list);
        // }

        if (!subtyping) {
            checkNot(expectedType.getClass(), actualType);
            if (!expectedType.equals(actualType)) {
                System.out.println(expectedType.getClass());
                System.out.println(actualType.getClass());
                throw new IllegalArgumentException("[ERROR_UNEXPECTED_TYPE_FOR_EXPRESSION]");
            }
        }
        return expectedType;
    }

    public boolean checkNot(Class<?> expectedType, Type actualType) {
        if (!(expectedType.isInstance(actualType))) {
            // System.out.println(expectedType.getName() + " " + actualType.getClass());
            if (actualType instanceof TypeSum) {
                throw new IllegalArgumentException("[ERROR_UNEXPECTED_INJECTION]");
            } else if (actualType instanceof TypeList) {
                throw new IllegalArgumentException("[ERROR_UNEXPECTED_LIST]");
            } else if (actualType instanceof TypeRef ref && ref.type_ instanceof TypeBottom) {
                throw new IllegalArgumentException("[ERROR_AMBIGUOUS_REFERENCE_TYPE]");
            } else if (actualType instanceof TypeThrow) {
                throw new IllegalArgumentException("[ERROR_AMBIGUOUS_THROW_TYPE]");
            } else if (actualType instanceof TypePanic) {
                throw new IllegalArgumentException("[ERROR_AMBIGUOUS_PANIC_TYPE]");
            } else if (actualType instanceof TypeVariant) {
                throw new IllegalArgumentException("[ERROR_AMBIGUOUS_VARIANT_TYPE]");
            }
            return false;
        }
        return true;
    }

    public void checkSubtype(Type expectedType, Type actualType) {
        System.out.println("Subtype check ");
        System.out.println(expectedType.getClass());
        System.out.println(actualType.getClass());

        if (recon) {

        }

        if (expectedType.equals(actualType) || (expectedType instanceof TypeTop || actualType instanceof TypeBottom)) {
            System.out.println("NoSub");
            return;
        } else if (expectedType instanceof TypeList expectedList && actualType instanceof TypeList actualList) {
            System.out.println("SubList");
            checkSubtype(expectedList.type_, actualList.type_);
            System.out.println("SubListOut");
            return;
        } else if (expectedType instanceof TypeSum expectedSum && actualType instanceof TypeSum actualSum) {
            System.out.println("SubSum");
            checkSubtype(expectedSum.type_1, actualSum.type_1);
            checkSubtype(expectedSum.type_2, actualSum.type_2);
            System.out.println("SubSumOut");
            return;
        } else if (expectedType instanceof TypeRef expectedRef && actualType instanceof TypeRef actualRef) {
            System.out.println("SubRef");
            checkSubtype(expectedRef.type_, actualRef.type_);
            checkSubtype(actualRef.type_, expectedRef.type_);
            System.out.println("SubRefOut");
            return;
        } else if (expectedType instanceof TypeRecord expectedRecord && actualType instanceof TypeRecord actualRecord) {
            System.out.println("SubRecord");
            Map<String, Type> expected = new HashMap<>();
            Map<String, Type> actual = new HashMap<>();

            for (RecordFieldType i : expectedRecord.listrecordfieldtype_) {
                // i.accept(new RecordFieldTypeVisitor(), null);
                i.accept((a, b) -> {
                    expected.put(a.stellaident_, a.type_);
                    return null;
                }, null);
            }
            for (RecordFieldType i : actualRecord.listrecordfieldtype_) {
                // i.accept(new RecordFieldTypeVisitor(), null);
                i.accept((a, b) -> {
                    actual.put(a.stellaident_, a.type_);
                    return null;
                }, null);
            }

            for (var i : expected.entrySet()) {
                String ident = i.getKey();
                // Type type = i.getValue();
                if (!actual.containsKey(ident)) {
                    throw new IllegalArgumentException("[ERROR_MISSING_RECORD_FIELDS]");
                }

                // Type subTyep = actual.get(ident);
                // // checkSubtype(type, subTyep);
                // checkSubtype(subTyep, type);
            }
            for (var i : expected.entrySet()) {
                String ident = i.getKey();
                Type type = i.getValue();
                if (!actual.containsKey(ident)) {
                    throw new IllegalArgumentException("[ERROR_MISSING_RECORD_FIELDS]");
                }

                Type subTyep = actual.get(ident);
                // checkSubtype(type, subTyep);
                checkSubtype(subTyep, type);
            }
            System.out.println("SubRecordOut");
            return;
        } else if (expectedType instanceof TypeTuple expectedTuple && actualType instanceof TypeTuple actualTuple) {
            System.out.println("SubTuple");
            int idx = 0;
            for (Type i : expectedTuple.listtype_) {
                checkSubtype(i, actualTuple.listtype_.get(idx));
                idx++;
            }
            System.out.println("SubTupleOut");
            return;
        } else if (expectedType instanceof TypeFun expectedFun && actualType instanceof TypeFun actualFun) {
            System.out.println("SubFun ");
            checkSubtype(expectedFun.type_, actualFun.type_);
            int idx = 0;
            for (Type i : expectedFun.listtype_) {
                checkSubtype(actualFun.listtype_.get(idx), i);
                idx++;
            }
            System.out.println("SubFunOut");
            return;
        } else if (expectedType instanceof TypeVariant expectedVariant
                && actualType instanceof TypeVariant actualVariant) {
            System.out.println("SubVariant");
            Map<String, Type> expectedMap = expectedVariant.listvariantfieldtype_.stream()
                    .map(AVariantFieldType.class::cast).collect(
                            Collectors.toMap(a -> a.stellaident_, a -> {
                                if (a.optionaltyping_ instanceof NoTyping)
                                    return new TypeBottom();
                                return ((SomeTyping) a.optionaltyping_).type_;
                            }));
            Map<String, Type> actualMap = actualVariant.listvariantfieldtype_.stream()
                    .map(AVariantFieldType.class::cast).collect(
                            Collectors.toMap(a -> a.stellaident_, a -> {
                                if (a.optionaltyping_ instanceof NoTyping)
                                    return new TypeBottom();
                                return ((SomeTyping) a.optionaltyping_).type_;
                            }));

            for (var i : actualMap.entrySet()) {
                String ident = i.getKey();
                Type type = i.getValue();

                if (!expectedMap.containsKey(ident)) {
                    throw new IllegalArgumentException("[ERROR_UNEXPECTED_VARIANT_LABEL]");
                }

                Type type2 = expectedMap.get(ident);
                checkSubtype(type2, type);
            }
            System.out.println("SubVariantOut");
            return;
        }
        throw new IllegalArgumentException("[ERROR_UNEXPECTED_SUBTYPE]");

    }
}
