package ycitss.content;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.test.AndroidTestCase;
import android.util.Log;

public class TestContact extends AndroidTestCase {
	private static final String TAG = "TestContact";

	// 获取通讯录中所有联系人
	public void testGetContact() {
		StringBuffer sb = new StringBuffer();
		ContentResolver contentResolver = this.getContext()
				.getContentResolver();
		Uri uri = Uri.parse("content://com.android.contacts/contacts");
		Cursor cursor = contentResolver.query(uri, null, null, null, null);
		while (cursor.moveToNext()) {
			String contactId = cursor.getString(cursor
					.getColumnIndex(ContactsContract.Contacts._ID));
			String name = cursor.getString(cursor
					.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
			sb.append("contactId=").append(contactId).append(",name=").append(
					name);
			Cursor phones = contentResolver.query(
					ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
					ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = "
							+ contactId, null, null);
			while (phones.moveToNext()) {
				String phone = phones
						.getString(phones
								.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
				sb.append(",phone=").append(phone);

			}
			Cursor emails = contentResolver.query(
					ContactsContract.CommonDataKinds.Email.CONTENT_URI, null,
					ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = "
							+ contactId, null, null);
			while (emails.moveToNext()) {
				String email = emails
						.getString(phones
								.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
				sb.append(",email=").append(email);
			}
			Log.i(TAG, sb.toString());
		}
	}

	/**
	 * 首先想RawContacts.CONTENT URI 执行一个空值插入，目的是为了获取返回的rawContactId
	 * 这是后面插入data表的依据，只有执行空值插入，才能使插入的联系人在通讯录里面可见
	 */
	public void testInsert() {
		ContentValues values = new ContentValues();
		// 首先想RawContacts.CONTENT_URI执行一个空值插入，目的似乎或偶去系统返回的rawContactId
		Uri rawContactUri = this.getContext().getContentResolver().insert(
				RawContacts.CONTENT_URI, values);
		long rawContactId = ContentUris.parseId(rawContactUri);
		// 往data表入姓名数据
		values.clear();
		values.put(Data.RAW_CONTACT_ID, rawContactId);
		values.put(Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE);
		values.put(StructuredName.GIVEN_NAME, "小样");
		this.getContext().getContentResolver().insert(
				android.provider.ContactsContract.Data.CONTENT_URI, values);

		// 往data表入电话数据
		values.clear();
		values.put(Data.RAW_CONTACT_ID, rawContactId);
		values.put(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE);
		values.put(Phone.NUMBER, "12345678901");
		values.put(Phone.TYPE, Phone.TYPE_MOBILE);
		this.getContext().getContentResolver().insert(
				android.provider.ContactsContract.Data.CONTENT_URI, values);

		// 往data表入Email数据
		values.clear();
		values.put(Data.RAW_CONTACT_ID, rawContactId);
		values.put(Data.MIMETYPE, Email.CONTENT_ITEM_TYPE);
		values.put(Email.DATA, "ycitdd@126.com");
		values.put(Email.TYPE, Phone.TYPE_WORK);
		this.getContext().getContentResolver().insert(
				android.provider.ContactsContract.Data.CONTENT_URI, values);

	}

	public void testSave() throws RemoteException,
			OperationApplicationException {
		// 文件位置：reference\android\provider\ContactsContract.RawContacts.html
		ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
		int rawContactInsertIndex = 0;
		ops.add(ContentProviderOperation.newInsert(RawContacts.CONTENT_URI)
				.withValue(RawContacts.ACCOUNT_TYPE, null).withValue(
						RawContacts.ACCOUNT_NAME, null).build());
		// 文件位置:reference\android\provider\ContactsContract.Data.html
		ops.add(ContentProviderOperation.newInsert(
				android.provider.ContactsContract.Data.CONTENT_URI)
				.withValueBackReference(Data.RAW_CONTACT_ID,
						rawContactInsertIndex).withValue(Data.MIMETYPE,
						StructuredName.CONTENT_ITEM_TYPE).withValue(
						StructuredName.GIVEN_NAME, "赵薇").build());

		ops.add(ContentProviderOperation.newInsert(
				android.provider.ContactsContract.Data.CONTENT_URI)
				.withValueBackReference(Data.RAW_CONTACT_ID,
						rawContactInsertIndex).withValue(Data.RAW_CONTACT_ID,
						rawContactInsertIndex).withValue(Data.MIMETYPE,
						Phone.CONTENT_ITEM_TYPE).withValue(Phone.TYPE,
						Phone.TYPE_MOBILE).withValue(Phone.LABEL, "手机号")
				.withValue(Phone.NUMBER, "13609221912").build());

		ops.add(ContentProviderOperation.newInsert(
				android.provider.ContactsContract.Data.CONTENT_URI)
				.withValueBackReference(Data.RAW_CONTACT_ID,
						rawContactInsertIndex).withValue(Data.MIMETYPE,
						Email.CONTENT_ITEM_TYPE).withValue(Email.DATA,
						"zhaowei@126.com").withValue(Email.TYPE,
						Email.TYPE_WORK).build());
		ContentProviderResult[] results = this.getContext()
				.getContentResolver().applyBatch(ContactsContract.AUTHORITY,
						ops);
		for (ContentProviderResult result : results) {
			Log.i(TAG, result.uri.toString());
		}

	}
}
