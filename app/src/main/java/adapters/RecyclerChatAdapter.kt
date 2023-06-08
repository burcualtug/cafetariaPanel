package adapters

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.marginLeft
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.cafetariapanel.R
import com.google.firebase.auth.FirebaseAuth
import datamodels.Chat


class RecyclerChatAdapter : RecyclerView.Adapter<RecyclerChatAdapter.ChatHolder>() {
    private val VIEW_TYPE_MESSAGE_SENT = 1
    private val VIEW_TYPE_MESSAGE_RECEIVED = 2

    private lateinit var chatLeft: TextView
    private lateinit var chatRight: TextView

    class ChatHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.recyclerViewTextView)
    }

    private val diffUtil = object : DiffUtil.ItemCallback<Chat>() {
        override fun areItemsTheSame(oldItem: Chat, newItem: Chat): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Chat, newItem: Chat): Boolean {
            return oldItem == newItem
        }

    }

    private val recyclerListDiffer = AsyncListDiffer(this, diffUtil)

    var chats: List<Chat>
        get() = recyclerListDiffer.currentList
        set(value) = recyclerListDiffer.submitList(value)

    override fun getItemViewType(position: Int): Int {
        val chat = chats.get(position)

        if(chat.user == FirebaseAuth.getInstance().currentUser?.email.toString()) {
            return VIEW_TYPE_MESSAGE_SENT
        } else {
            return VIEW_TYPE_MESSAGE_RECEIVED
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatHolder {
        /*val layoutParamsLeft = R.layout.chat_layout_left as ConstraintLayout.LayoutParams
        val layoutParamsRight = R.layout.chat_layout_right as ConstraintLayout.LayoutParams*/
       /* val itemView = LayoutInflater.from(parent.context).inflate(R.layout.chat_layout_left, parent, false)
        val layoutParams = itemView.layoutParams as RecyclerView.LayoutParams
        layoutParams.gravity = Gravity.END

        // Diğer düzenlemeleri yapabilirsiniz (ör. margin)

        itemView.layoutParams = layoutParams*/

        if(viewType == VIEW_TYPE_MESSAGE_RECEIVED) {

            val view = LayoutInflater.from(parent.context).inflate(R.layout.chat_layout_left, parent, false)
            val layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            layoutParams.gravity = Gravity.START
            view.layoutParams = layoutParams

            return ChatHolder(view)

        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.chat_layout_right, parent, false)
            val layoutParams = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_END)
            view.layoutParams = layoutParams

            return ChatHolder(view)
        }
    }

    override fun onBindViewHolder(holder: ChatHolder, position: Int) {


        val textView = holder.itemView.findViewById<TextView>(R.id.recyclerViewTextView)
        textView.text = "${chats.get(position).text}"

        //"${chats.get(position).user} : ${chats.get(position).text}"
    }

    override fun getItemCount(): Int {
        return chats.size
    }
}