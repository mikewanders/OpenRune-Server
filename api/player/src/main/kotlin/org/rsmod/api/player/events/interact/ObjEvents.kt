package org.rsmod.api.player.events.interact

import dev.openrune.definition.type.widget.ComponentType
import dev.openrune.types.ItemServerType
import org.rsmod.events.EventBus
import org.rsmod.game.obj.Obj

public sealed class ObjEvents {
    public sealed class Op(public val obj: Obj) : OpEvent(obj.type.toLong())

    public class Op1(obj: Obj) : Op(obj)

    public class Op2(obj: Obj) : Op(obj)

    public class Op3(obj: Obj) : Op(obj)

    public class Op4(obj: Obj) : Op(obj)

    public class Op5(obj: Obj) : Op(obj)

    public class Op6(obj: Obj) : Op(obj)

    public sealed class Ap(public val obj: Obj) : ApEvent(obj.type.toLong())

    public class Ap1(obj: Obj) : Ap(obj)

    public class Ap2(obj: Obj) : Ap(obj)

    public class Ap3(obj: Obj) : Ap(obj)

    public class Ap4(obj: Obj) : Ap(obj)

    public class Ap5(obj: Obj) : Ap(obj)
}

public sealed class ObjContentEvents {
    public sealed class Op(public val obj: Obj, contentGroup: Int) : OpEvent(contentGroup.toLong())

    public class Op1(obj: Obj, content: Int) : Op(obj, content)

    public class Op2(obj: Obj, content: Int) : Op(obj, content)

    public class Op3(obj: Obj, content: Int) : Op(obj, content)

    public class Op4(obj: Obj, content: Int) : Op(obj, content)

    public class Op5(obj: Obj, content: Int) : Op(obj, content)

    public class Op6(obj: Obj, content: Int) : Op(obj, content)

    public sealed class Ap(public val obj: Obj, contentGroup: Int) : ApEvent(contentGroup.toLong())

    public class Ap1(obj: Obj, content: Int) : Ap(obj, content)

    public class Ap2(obj: Obj, content: Int) : Ap(obj, content)

    public class Ap3(obj: Obj, content: Int) : Ap(obj, content)

    public class Ap4(obj: Obj, content: Int) : Ap(obj, content)

    public class Ap5(obj: Obj, content: Int) : Ap(obj, content)
}

public sealed class ObjDefaultEvents {
    public sealed class Op(public val obj: Obj) : OpDefaultEvent()

    public class Op1(obj: Obj) : Op(obj)

    public class Op2(obj: Obj) : Op(obj)

    public class Op3(obj: Obj) : Op(obj)

    public class Op4(obj: Obj) : Op(obj)

    public class Op5(obj: Obj) : Op(obj)

    public class Op6(obj: Obj) : Op(obj)

    public sealed class Ap(public val obj: Obj) : ApDefaultEvent()

    public class Ap1(obj: Obj) : Ap(obj)

    public class Ap2(obj: Obj) : Ap(obj)

    public class Ap3(obj: Obj) : Ap(obj)

    public class Ap4(obj: Obj) : Ap(obj)

    public class Ap5(obj: Obj) : Ap(obj)
}

/**
 * A component (usually a spell) used on a ground obj.
 *
 * @param objType The selected inventory obj, if the component was used from an inventory slot.
 */
public class ObjTEvents {
    public class Op(
        public val obj: Obj,
        public val objType: ItemServerType?,
        public val comsub: Int,
        component: ComponentType,
    ) : OpEvent(EventBus.composeLongKey(obj.type, component.packed))

    public class Ap(
        public val obj: Obj,
        public val objType: ItemServerType?,
        public val comsub: Int,
        component: ComponentType,
    ) : ApEvent(EventBus.composeLongKey(obj.type, component.packed))
}

public class ObjTDefaultEvents {
    public class Op(
        public val obj: Obj,
        public val objType: ItemServerType?,
        public val comsub: Int,
        component: ComponentType,
    ) : OpEvent(component.packed.toLong())

    public class Ap(
        public val obj: Obj,
        public val objType: ItemServerType?,
        public val comsub: Int,
        component: ComponentType,
    ) : ApEvent(component.packed.toLong())
}
